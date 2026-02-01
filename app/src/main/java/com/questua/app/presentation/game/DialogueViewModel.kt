package com.questua.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Choice
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.usecase.exploration.GetCharacterDetailsUseCase
import com.questua.app.domain.usecase.gameplay.CompleteQuestUseCase
import com.questua.app.domain.usecase.gameplay.LoadSceneEngineUseCase
import com.questua.app.domain.usecase.gameplay.SubmitDialogueResponseUseCase
import com.questua.app.domain.usecase.quest.StartQuestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DialogueState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Estado da Sessão
    val userQuestId: String? = null,
    val questProgress: Float = 0f,
    val isQuestCompleted: Boolean = false,

    // Cena Atual
    val currentDialogue: SceneDialogue? = null,
    val speaker: CharacterEntity? = null,

    // Interação
    val userInput: String = "",
    val isSubmitting: Boolean = false,
    val feedbackState: FeedbackState = FeedbackState.None
)

sealed class FeedbackState {
    object None : FeedbackState()
    data class Success(val message: String?) : FeedbackState()
    data class Error(val message: String?) : FeedbackState()
}

@HiltViewModel
class DialogueViewModel @Inject constructor(
    private val startQuestUseCase: StartQuestUseCase,
    private val loadSceneEngineUseCase: LoadSceneEngineUseCase,
    private val submitDialogueResponseUseCase: SubmitDialogueResponseUseCase,
    private val completeQuestUseCase: CompleteQuestUseCase,
    private val getCharacterDetailsUseCase: GetCharacterDetailsUseCase,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DialogueState())
    val state: StateFlow<DialogueState> = _state.asStateFlow()

    private val questId: String = checkNotNull(savedStateHandle["questId"])

    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (userId != null) {
                    startQuest(userId)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Usuário não autenticado")
                }
            }
        }
    }

    private fun startQuest(userId: String) {
        viewModelScope.launch {
            startQuestUseCase(userId, questId).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        val userQuest = result.data!!
                        _state.value = _state.value.copy(
                            userQuestId = userQuest.id,
                            questProgress = userQuest.percentComplete
                        )
                        loadScene(userQuest.lastDialogueId)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun loadScene(dialogueId: String) {
        viewModelScope.launch {
            loadSceneEngineUseCase(dialogueId).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        val dialogue = result.data!!
                        _state.value = _state.value.copy(
                            currentDialogue = dialogue,
                            userInput = "",
                            feedbackState = FeedbackState.None,
                            isLoading = false,
                            speaker = null // Limpa speaker anterior enquanto carrega o novo
                        )

                        // Busca detalhes do personagem se houver ID
                        dialogue.speakerCharacterId?.let { loadSpeaker(it) }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun loadSpeaker(characterId: String) {
        viewModelScope.launch {
            getCharacterDetailsUseCase(characterId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(speaker = result.data)
                }
            }
        }
    }

    fun onUserInputChange(text: String) {
        _state.value = _state.value.copy(userInput = text)
    }

    fun onChoiceSelected(choice: Choice) {
        val current = _state.value.currentDialogue ?: return

        // Se a escolha deve ser validada (User Response), submete. Se for só navegação, avança.
        if (current.expectsUserResponse) {
            submitAnswer(choice.text, choice.nextDialogueId)
        } else {
            // Apenas ramificação de narrativa sem validação de "certo/errado"
            advanceToNext(choice.nextDialogueId ?: current.nextDialogueId)
        }
    }

    fun onSubmitText() {
        val text = _state.value.userInput
        if (text.isNotBlank()) {
            submitAnswer(text, null)
        }
    }

    private fun submitAnswer(answer: String, nextIdOverride: String?) {
        val current = _state.value.currentDialogue ?: return
        val userQuestId = _state.value.userQuestId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)

            submitDialogueResponseUseCase(userQuestId, current.id, answer).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        val isCorrect = result.data ?: true

                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            feedbackState = if (isCorrect) FeedbackState.Success("Correto!") else FeedbackState.Error("Tente novamente.")
                        )

                        if (isCorrect) {
                            delay(1000)
                            // Se tiver override (da escolha), usa ele. Senão usa o padrão da cena.
                            val nextId = nextIdOverride ?: current.nextDialogueId
                            advanceToNext(nextId)
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            feedbackState = FeedbackState.Error(result.message)
                        )
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun onContinue() {
        val current = _state.value.currentDialogue ?: return
        advanceToNext(current.nextDialogueId)
    }

    private fun advanceToNext(nextDialogueId: String?) {
        if (!nextDialogueId.isNullOrBlank()) {
            loadScene(nextDialogueId)
        } else {
            completeQuest()
        }
    }

    private fun completeQuest() {
        val userQuestId = _state.value.userQuestId ?: return
        viewModelScope.launch {
            completeQuestUseCase(userQuestId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(isQuestCompleted = true)
                }
            }
        }
    }
}