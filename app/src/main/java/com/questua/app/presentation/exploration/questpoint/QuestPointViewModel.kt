package com.questua.app.presentation.exploration.questpoint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.GameRepository
import com.questua.app.domain.usecase.exploration.GetQuestPointDetailsUseCase
import com.questua.app.domain.usecase.exploration.GetQuestPointQuestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestItemState(
    val quest: Quest,
    val status: QuestStatus,
    val userScore: Int = 0
)

enum class QuestStatus {
    LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED
}

data class QuestPointState(
    val isLoading: Boolean = false,
    val questPoint: QuestPoint? = null,
    val quests: List<QuestItemState> = emptyList(),
    val totalProgressPercent: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class QuestPointViewModel @Inject constructor(
    private val getQuestPointDetailsUseCase: GetQuestPointDetailsUseCase,
    private val getQuestPointQuestsUseCase: GetQuestPointQuestsUseCase,
    private val gameRepository: GameRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuestPointState())
    val state = _state.asStateFlow()

    private val pointId: String? = savedStateHandle["pointId"]

    init {
        loadData()
    }

    fun loadData() {
        if (pointId == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenManager.userId.first() ?: return@launch

            // 1. Carrega Detalhes do Ponto
            getQuestPointDetailsUseCase(pointId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(questPoint = result.data)
                        // 2. Após carregar o ponto, carrega as Quests
                        loadQuests(pointId, userId)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private suspend fun loadQuests(pointId: String, userId: String) {
        getQuestPointQuestsUseCase(pointId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    val rawQuests = result.data?.sortedBy { it.orderIndex } ?: emptyList()
                    val questsWithStatus = processQuestStatus(rawQuests, userId)

                    val totalQuests = questsWithStatus.size
                    val completedQuests = questsWithStatus.count { it.status == QuestStatus.COMPLETED }
                    val progress = if (totalQuests > 0) (completedQuests.toFloat() / totalQuests) else 0f

                    _state.value = _state.value.copy(
                        isLoading = false,
                        quests = questsWithStatus,
                        totalProgressPercent = progress
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private suspend fun processQuestStatus(quests: List<Quest>, userId: String): List<QuestItemState> {
        val resultList = mutableListOf<QuestItemState>()
        var previousCompleted = true // A primeira quest é desbloqueada por padrão se a anterior (inexistente) for "completa"

        for (quest in quests) {
            // Verifica o status individual de cada quest para o usuário
            // Nota: Isso faz chamadas sequenciais. Em um cenário ideal, buscaríamos "getUserQuestsByPoint" de uma vez.
            var status = QuestStatus.LOCKED
            var score = 0

            try {
                val userQuestResource = gameRepository.getUserQuestStatus(quest.id, userId).first()
                if (userQuestResource is Resource.Success) {
                    val uq = userQuestResource.data
                    if (uq != null) {
                        status = if (uq.status == ProgressStatus.COMPLETED) QuestStatus.COMPLETED else QuestStatus.IN_PROGRESS
                        score = uq.score
                    }
                }
            } catch (e: Exception) {
                // Erro ou não encontrado, mantém lógica padrão
            }

            // Se não tem registro de progresso, verifica se pode estar disponível
            if (status == QuestStatus.LOCKED) {
                if (previousCompleted) {
                    status = QuestStatus.AVAILABLE
                }
            }

            resultList.add(QuestItemState(quest, status, score))

            // Atualiza flag para a próxima iteração
            previousCompleted = (status == QuestStatus.COMPLETED)
        }
        return resultList
    }
}