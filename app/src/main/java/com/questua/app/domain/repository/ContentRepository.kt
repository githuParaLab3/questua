package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getCities(languageId: String): Flow<Resource<List<City>>>
    fun getCityDetails(cityId: String): Flow<Resource<City>>
    fun getQuestPoints(cityId: String): Flow<Resource<List<QuestPoint>>>
    fun getQuestPointDetails(pointId: String): Flow<Resource<QuestPoint>>
    fun getQuests(questPointId: String): Flow<Resource<List<Quest>>>
    fun getQuestDetails(questId: String): Flow<Resource<Quest>>
    fun getSceneDialogue(dialogueId: String): Flow<Resource<SceneDialogue>>

    fun getUnlockPreview(contentId: String, type: String): Flow<Resource<UnlockRequirement>>
    fun unlockContent(contentId: String, type: String): Flow<Resource<Boolean>>

    fun createCity(city: City): Flow<Resource<City>>
    fun updateCity(city: City): Flow<Resource<City>>
    fun deleteContent(contentId: String, type: String): Flow<Resource<Unit>>
    fun publishContent(contentId: String, type: String): Flow<Resource<Boolean>>
    fun archiveContent(contentId: String, type: String): Flow<Resource<Boolean>>
}