package com.questua.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.usecase.quest.GetQuestIntroUseCase
import com.questua.app.domain.usecase.quest.StartQuestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- CLASSE DE EVENTOS (Deve estar fora da class ViewModel) ---
sealed class QuestIntroUiEvent {
    data class NavigateToGame(val questId: String) : QuestIntroUiEvent()
    data class ShowError(val message: String) : QuestIntroUiEvent()
}

data class QuestIntroState(
    val isLoading: Boolean = false,
    val quest: Quest? = null,
    val userQuest: UserQuest? = null,
    val questPoint: QuestPoint? = null,
    val error: String? = null
)

@HiltViewModel
class QuestIntroViewModel @Inject constructor(
    private val getQuestIntroUseCase: GetQuestIntroUseCase,
    private val startQuestUseCase: StartQuestUseCase,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuestIntroState())
    val state = _state.asStateFlow()

    // Canal de comunicação para a UI (Navegação, Toasts)
    private val _uiEvent = Channel<QuestIntroUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val questId: String? = savedStateHandle["questId"]

    init {
        loadData()
    }

    fun loadData() {
        if (questId == null) return

        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            // Busca os dados da Quest e do UserQuest (progresso)
            getQuestIntroUseCase(questId, userId).collect { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            quest = result.data?.quest,
                            userQuest = result.data?.userQuest,
                            questPoint = result.data?.questPoint
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    // Método corrigido: NÃO recebe parâmetros
    fun onStartQuestClicked() {
        val quest = state.value.quest ?: return

        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            // Se a quest já foi iniciada anteriormente, apenas navega
            if (state.value.userQuest != null) {
                _uiEvent.send(QuestIntroUiEvent.NavigateToGame(quest.id))
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            // Inicia a quest no backend
            startQuestUseCase(userId, quest.id).collect { result ->
                when(result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false)
                        // Sucesso: Envia evento para navegar
                        _uiEvent.send(QuestIntroUiEvent.NavigateToGame(quest.id))
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                        _uiEvent.send(QuestIntroUiEvent.ShowError(result.message ?: "Erro desconhecido"))
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
}