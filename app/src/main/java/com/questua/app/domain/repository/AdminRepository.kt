package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // --- AI Generation ---
    fun generateQuestPoint(cityId: String, theme: String): Flow<Resource<QuestPoint>>
    fun generateQuest(questPointId: String, context: String, difficulty: Int): Flow<Resource<Quest>>
    fun generateCharacter(archetype: String): Flow<Resource<CharacterEntity>>
    fun generateDialogue(speakerId: String, context: String, inputMode: String): Flow<Resource<SceneDialogue>>
    fun generateAchievement(trigger: String, difficulty: String): Flow<Resource<Achievement>>

    // --- AI Logs ---
    fun getAiLogs(page: Int, size: Int): Flow<Resource<List<AiGenerationLog>>>

    // --- Content Management (CRUD) ---
    // City
    fun createCity(city: City): Flow<Resource<City>>
    fun updateCity(city: City): Flow<Resource<City>>
    fun deleteCity(cityId: String): Flow<Resource<Unit>>

    // QuestPoint
    fun createQuestPoint(questPoint: QuestPoint): Flow<Resource<QuestPoint>>
    fun updateQuestPoint(questPoint: QuestPoint): Flow<Resource<QuestPoint>>
    fun deleteQuestPoint(questPointId: String): Flow<Resource<Unit>>

    // Quest
    fun createQuest(quest: Quest): Flow<Resource<Quest>>
    fun updateQuest(quest: Quest): Flow<Resource<Quest>>
    fun deleteQuest(questId: String): Flow<Resource<Unit>>

    // Character Entity
    fun createCharacter(character: CharacterEntity): Flow<Resource<CharacterEntity>>
    fun updateCharacter(character: CharacterEntity): Flow<Resource<CharacterEntity>>
    fun deleteCharacter(characterId: String): Flow<Resource<Unit>>

    // --- Feedback & Moderation ---
    fun getAllReports(page: Int, size: Int): Flow<Resource<List<Report>>>
    fun updateReport(report: Report): Flow<Resource<Report>>

    // --- User Management ---
    fun getAllUsers(page: Int, size: Int): Flow<Resource<List<UserAccount>>>

    // --- Sales ---
    fun getAllTransactions(page: Int, size: Int): Flow<Resource<List<TransactionRecord>>>
}