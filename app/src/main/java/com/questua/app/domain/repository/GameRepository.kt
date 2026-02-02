package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.model.UserQuest
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun startQuest(userId: String, questId: String): Flow<Resource<UserQuest>>
    fun retryQuest(userQuestId: String): Flow<Resource<UserQuest>>
    fun getNextDialogue(userQuestId: String): Flow<Resource<String>> // Retorna ID do próximo diálogo
    fun completeQuest(userQuestId: String): Flow<Resource<UserQuest>>
    fun getUserQuestStatus(questId: String, userId: String): Flow<Resource<UserQuest>>
    fun getQuestById(questId: String): Flow<Resource<Quest>>
    fun getDialogue(dialogueId: String): Flow<Resource<SceneDialogue>>

    fun submitResponse(userQuestId: String, dialogueId: String, answer: String): Flow<Resource<UserQuest>>
}