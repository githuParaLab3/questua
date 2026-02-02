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
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
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
    private val getUserStatsUseCase: GetUserStatsUseCase,
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
            val userId = tokenManager.userId.first() ?: return@launch

            // 1. Carrega detalhes estáticos do Ponto
            getQuestPointDetailsUseCase(pointId).collect { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(questPoint = result.data)
                }
            }

            // 2. OBSERVAÇÃO CONTÍNUA (COMBINE)
            // Combinamos a lista de quests com os status do usuário.
            // Se qualquer um dos dois mudar (ex: atualização da rede), a UI recalcula.
            combine(
                getQuestPointQuestsUseCase(pointId),
                getUserStatsUseCase(userId)
            ) { questsRes, statsRes ->

                val isLoading = questsRes is Resource.Loading || statsRes is Resource.Loading
                var error: String? = null

                if (questsRes is Resource.Error) error = questsRes.message
                if (statsRes is Resource.Error) error = statsRes.message

                val rawQuests = questsRes.data?.sortedBy { it.orderIndex } ?: emptyList()

                // Pega a lista de IDs desbloqueados (garante lista vazia se loading/erro)
                val unlockedQuestIds = statsRes.data?.unlockedContent?.quests ?: emptyList()

                // Processa o status combinando tudo
                val questsWithStatus = processQuestStatus(rawQuests, userId, unlockedQuestIds)

                val totalQuests = questsWithStatus.size
                val completedQuests = questsWithStatus.count { it.status == QuestStatus.COMPLETED }
                val progress = if (totalQuests > 0) (completedQuests.toFloat() / totalQuests) else 0f

                QuestPointState(
                    isLoading = isLoading && rawQuests.isEmpty(), // Só mostra loading se não tiver dados
                    questPoint = _state.value.questPoint,
                    quests = questsWithStatus,
                    totalProgressPercent = progress,
                    error = error
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private suspend fun processQuestStatus(
        quests: List<Quest>,
        userId: String,
        unlockedQuestIds: List<String>
    ): List<QuestItemState> {
        val resultList = mutableListOf<QuestItemState>()

        // Mantém a lógica híbrida: A primeira é sempre liberada se não tiver anterior
        var isPreviousCompleted = true

        for (quest in quests) {
            var status = QuestStatus.LOCKED
            var userScore = 0

            // Lógica: Backend autoriza OU Sequência local autoriza
            val isUnlockedByBackend = unlockedQuestIds.any { it.equals(quest.id, ignoreCase = true) }
            val shouldBeUnlocked = isUnlockedByBackend || isPreviousCompleted

            if (shouldBeUnlocked) {
                try {
                    // Busca estado individual (XP/Status real)
                    val userQuestResource = gameRepository.getUserQuestStatus(quest.id, userId).first()

                    if (userQuestResource is Resource.Success && userQuestResource.data != null) {
                        val uq = userQuestResource.data
                        status = if (uq.status == ProgressStatus.COMPLETED) QuestStatus.COMPLETED else QuestStatus.IN_PROGRESS
                        userScore = uq.score
                    } else {
                        status = QuestStatus.AVAILABLE
                    }
                } catch (e: Exception) {
                    status = QuestStatus.AVAILABLE
                }
            } else {
                status = QuestStatus.LOCKED
            }

            resultList.add(QuestItemState(quest, status, userScore))
            isPreviousCompleted = (status == QuestStatus.COMPLETED)
        }

        return resultList
    }
}