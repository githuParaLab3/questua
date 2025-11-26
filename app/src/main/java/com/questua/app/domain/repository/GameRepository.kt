package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserQuest
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun startQuest(userId: String, questId: String): Flow<Resource<UserQuest>>
    fun retryQuest(userQuestId: String): Flow<Resource<UserQuest>>
    fun getNextDialogue(userQuestId: String): Flow<Resource<String>> // Retorna ID do próximo diálogo
    fun submitResponse(userQuestId: String, questionId: String, answer: String): Flow<Resource<Boolean>>
    fun completeQuest(userQuestId: String): Flow<Resource<UserQuest>>
}