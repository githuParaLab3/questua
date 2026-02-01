package com.questua.app.domain.usecase.quest

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class QuestIntroData(
    val quest: Quest,
    val userQuest: UserQuest?,
    val questPoint: QuestPoint?
)

class GetQuestIntroUseCase @Inject constructor(
    private val contentRepository: ContentRepository,
    private val gameRepository: GameRepository
) {
    operator fun invoke(questId: String, userId: String): Flow<Resource<QuestIntroData>> = flow {
        emit(Resource.Loading())

        try {
            contentRepository.getQuestDetails(questId).collect { questResult ->
                when (questResult) {
                    is Resource.Success -> {
                        val quest = questResult.data!!

                        val userQuestFlow = gameRepository.getUserQuestStatus(questId, userId)
                        val questPointFlow = contentRepository.getQuestPointDetails(quest.questPointId)

                        combine(userQuestFlow, questPointFlow) { userQuestRes, questPointRes ->
                            val userQuest = if (userQuestRes is Resource.Success) userQuestRes.data else null
                            val questPoint = if (questPointRes is Resource.Success) questPointRes.data else null

                            QuestIntroData(quest, userQuest, questPoint)
                        }.collect { data ->
                            emit(Resource.Success(data))
                        }
                    }
                    is Resource.Error -> emit(Resource.Error(questResult.message ?: "Erro ao carregar dados"))
                    is Resource.Loading -> {}
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erro desconhecido"))
        }
    }
}