package com.questua.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.SkillAssessment
import com.questua.app.domain.repository.GameRepository
import com.questua.app.domain.usecase.exploration.GetQuestDetailsUseCase
import com.questua.app.domain.usecase.exploration.GetQuestPointQuestsUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestResultState(
    val isLoading: Boolean = false,
    val questId: String = "",
    val questPointId: String = "",
    val xpEarned: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val accuracy: Int = 0,
    val overallAssessment: List<SkillAssessment> = emptyList(),
    val nextQuestId: String? = null
)

@HiltViewModel
class QuestResultViewModel @Inject constructor(
    private val getQuestDetailsUseCase: GetQuestDetailsUseCase,
    private val getQuestPointQuestsUseCase: GetQuestPointQuestsUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val gameRepository: GameRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuestResultState())
    val state: StateFlow<QuestResultState> = _state.asStateFlow()

    init {
        val questId: String = checkNotNull(savedStateHandle["questId"])
        val xpEarned: Int = savedStateHandle["xpEarned"] ?: 0
        val correctAnswers: Int = savedStateHandle["correctAnswers"] ?: 0
        val totalQuestions: Int = savedStateHandle["totalQuestions"] ?: 0

        val accuracy = if (totalQuestions > 0) {
            ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()
        } else 0

        _state.value = QuestResultState(
            isLoading = false,
            questId = questId,
            xpEarned = xpEarned,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            accuracy = accuracy
        )

        loadExtraData(questId)
    }

    private fun loadExtraData(questId: String) {
        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            // 1. Pega detalhes da quest atual para saber qual é o Ponto e a Ordem
            getQuestDetailsUseCase(questId).collect { questResource ->
                if (questResource is Resource.Success) {
                    val quest = questResource.data
                    val qpId = quest?.questPointId ?: ""
                    val currentOrder = quest?.orderIndex ?: 0

                    _state.value = _state.value.copy(questPointId = qpId)

                    // Carrega avaliação
                    fetchAssessment(questId, userId)

                    // 2. Monitora REATIVAMENTE se a próxima missão foi desbloqueada
                    monitorNextQuest(qpId, currentOrder, userId)
                }
            }
        }
    }

    private fun fetchAssessment(questId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Tenta buscar avaliação (pode demorar um pouco para ser gerada)
                val userQuestResult = gameRepository.getUserQuestStatus(questId, userId).first()
                if (userQuestResult is Resource.Success && userQuestResult.data != null) {
                    val assessment = userQuestResult.data.overallAssessment ?: emptyList()
                    if (assessment.isNotEmpty()) {
                        _state.value = _state.value.copy(overallAssessment = assessment)
                    }
                }
            } catch (e: Exception) {
                // Falha silenciosa
            }
        }
    }

    private fun monitorNextQuest(questPointId: String, currentOrder: Int, userId: String) {
        viewModelScope.launch {
            // COMBINE: Escuta alterações tanto na lista de quests quanto no status do usuário
            combine(
                getQuestPointQuestsUseCase(questPointId),
                getUserStatsUseCase(userId)
            ) { questsRes, statsRes ->
                var nextId: String? = null

                if (questsRes is Resource.Success && statsRes is Resource.Success) {
                    val allQuests = questsRes.data ?: emptyList()
                    val unlockedQuests = statsRes.data?.unlockedContent?.quests ?: emptyList()

                    // Encontra a próxima missão (index > atual)
                    val nextQuest = allQuests
                        .filter { it.orderIndex > currentOrder }
                        .minByOrNull { it.orderIndex }

                    // Verifica se o ID dela está na lista de desbloqueados do usuário
                    if (nextQuest != null && unlockedQuests.contains(nextQuest.id)) {
                        nextId = nextQuest.id
                    }
                }
                nextId
            }.collect { nextId ->
                // Atualiza o estado assim que a condição for satisfeita
                if (nextId != _state.value.nextQuestId) {
                    _state.value = _state.value.copy(nextQuestId = nextId)
                }
            }
        }
    }
}