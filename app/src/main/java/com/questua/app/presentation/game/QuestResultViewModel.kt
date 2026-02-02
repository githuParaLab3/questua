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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestResultState(
    val isLoading: Boolean = false, // Começa false pois já temos dados básicos
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
        // 1. Recupera dados IMEDIATOS da navegação (sem delay)
        val questId: String = checkNotNull(savedStateHandle["questId"])
        val xpEarned: Int = savedStateHandle["xpEarned"] ?: 0
        val correctAnswers: Int = savedStateHandle["correctAnswers"] ?: 0
        val totalQuestions: Int = savedStateHandle["totalQuestions"] ?: 0

        val accuracy = if (totalQuestions > 0) {
            ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()
        } else 0

        // 2. Atualiza o estado visual imediatamente (Adeus Loading Infinito)
        _state.value = QuestResultState(
            isLoading = false,
            questId = questId,
            xpEarned = xpEarned,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            accuracy = accuracy
        )

        // 3. Carrega dados complementares em segundo plano (Assessment, Próxima Missão)
        loadExtraData(questId)
    }

    private fun loadExtraData(questId: String) {
        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            // Busca detalhes da Quest para pegar o ID do Ponto (para o botão Voltar)
            getQuestDetailsUseCase(questId).collect { questResource ->
                if (questResource is Resource.Success) {
                    val quest = questResource.data
                    val qpId = quest?.questPointId ?: ""
                    val currentOrder = quest?.orderIndex ?: 0

                    // Atualiza o ID do ponto assim que disponível
                    _state.value = _state.value.copy(questPointId = qpId)

                    // Busca Assessment (Avaliação Detalhada)
                    launch {
                        try {
                            val userQuestResult = gameRepository.getUserQuestStatus(questId, userId).first()
                            if (userQuestResult is Resource.Success && userQuestResult.data != null) {
                                val assessment = userQuestResult.data.overallAssessment ?: emptyList()
                                if (assessment.isNotEmpty()) {
                                    _state.value = _state.value.copy(overallAssessment = assessment)
                                }
                            }
                        } catch (e: Exception) {
                            // Falha silenciosa no assessment, não trava a tela
                        }
                    }

                    // Verifica Próxima Missão
                    findNextQuest(qpId, currentOrder, userId)
                }
            }
        }
    }

    private suspend fun findNextQuest(questPointId: String, currentOrder: Int, userId: String) {
        try {
            val userStats = getUserStatsUseCase(userId).first()
            val unlockedQuests = if (userStats is Resource.Success) {
                userStats.data?.unlockedContent?.quests ?: emptyList()
            } else emptyList()

            getQuestPointQuestsUseCase(questPointId).collect { resource ->
                if (resource is Resource.Success) {
                    val quests = resource.data ?: emptyList()

                    val nextQuest = quests
                        .filter { it.orderIndex > currentOrder }
                        .minByOrNull { it.orderIndex }

                    if (nextQuest != null && unlockedQuests.contains(nextQuest.id)) {
                        _state.value = _state.value.copy(nextQuestId = nextQuest.id)
                    }
                }
            }
        } catch (e: Exception) {
            // Falha silenciosa
        }
    }
}