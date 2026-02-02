package com.questua.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Choice
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.usecase.exploration.GetCharacterDetailsUseCase
import com.questua.app.domain.usecase.gameplay.LoadSceneEngineUseCase
import com.questua.app.domain.usecase.gameplay.SubmitDialogueResponseUseCase
import com.questua.app.domain.usecase.quest.StartQuestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DialogueState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userQuestId: String? = null,
    val questProgress: Float = 0f,
    val isQuestCompleted: Boolean = false,
    val navigateToResult: Boolean = false,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Int = 0,
    val currentDialogue: SceneDialogue? = null,
    val speaker: CharacterEntity? = null,
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
                    _state.update { it.copy(isLoading = false, error = "Usuário não autenticado") }
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
                        _state.update {
                            it.copy(
                                userQuestId = userQuest.id,
                                questProgress = userQuest.percentComplete,
                                correctAnswers = userQuest.score,
                                xpEarned = userQuest.xpEarned,
                                totalQuestions = userQuest.responses?.size ?: 0
                            )
                        }

                        if (userQuest.status == ProgressStatus.COMPLETED) {
                            _state.update { it.copy(isQuestCompleted = true, navigateToResult = true) }
                        } else {
                            loadScene(userQuest.lastDialogueId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
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
                        _state.update {
                            it.copy(
                                currentDialogue = dialogue,
                                userInput = "",
                                feedbackState = FeedbackState.None,
                                isLoading = false,
                                speaker = null
                            )
                        }
                        dialogue.speakerCharacterId?.let { loadSpeaker(it) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
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
                    _state.update { it.copy(speaker = result.data) }
                }
            }
        }
    }

    fun onUserInputChange(text: String) {
        _state.update { it.copy(userInput = text) }
    }

    fun onSubmitText() {
        val text = _state.value.userInput
        if (text.isNotBlank()) {
            processInteraction(text)
        }
    }

    fun onChoiceSelected(choice: Choice) {
        processInteraction(choice.text)
    }

    fun onContinue() {
        // Envia um valor padrão para passar na validação @NotBlank do backend
        processInteraction("CONTINUE")
    }

    private fun processInteraction(answer: String) {
        val current = _state.value.currentDialogue ?: return
        val userQuestId = _state.value.userQuestId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            submitDialogueResponseUseCase(userQuestId, current.id, answer).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        val updatedQuest = result.data!!

                        val lastResponse = updatedQuest.responses?.lastOrNull()
                        val isCorrect = lastResponse?.correct ?: true
                        val feedbackText = lastResponse?.feedback
                        val showFeedback = current.expectsUserResponse

                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                correctAnswers = updatedQuest.score,
                                xpEarned = updatedQuest.xpEarned,
                                questProgress = updatedQuest.percentComplete,
                                totalQuestions = updatedQuest.responses?.size ?: (it.totalQuestions + 1)
                            )
                        }

                        if (showFeedback) {
                            val feedback = if (isCorrect) FeedbackState.Success("Correto!")
                            else FeedbackState.Error(feedbackText ?: "Incorreto!")
                            _state.update { it.copy(feedbackState = feedback) }
                            if (isCorrect) delay(1500)
                        }

                        if (updatedQuest.status == ProgressStatus.COMPLETED) {
                            _state.update { it.copy(isQuestCompleted = true, navigateToResult = true) }
                        } else {
                            loadScene(updatedQuest.lastDialogueId)
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                feedbackState = FeedbackState.Error(result.message)
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun onResultNavigationHandled() {
        _state.update { it.copy(navigateToResult = false) }
    }
}