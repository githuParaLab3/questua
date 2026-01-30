package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.enums.TargetType
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AdminRepository {
    fun generateQuestPoint(cityId: String, theme: String): Flow<Resource<QuestPoint>>
    fun generateQuest(questPointId: String, context: String, difficulty: Int): Flow<Resource<Quest>>
    fun generateCharacter(archetype: String): Flow<Resource<CharacterEntity>>
    fun generateDialogue(speakerId: String, context: String, inputMode: String): Flow<Resource<SceneDialogue>>
    fun generateAchievement(trigger: String, difficulty: String): Flow<Resource<Achievement>>

    fun getAiLogs(page: Int, size: Int): Flow<Resource<List<AiGenerationLog>>>

    fun createCity(city: City): Flow<Resource<City>>
    fun updateCity(city: City): Flow<Resource<City>>
    fun deleteCity(cityId: String): Flow<Resource<Unit>>

    fun createQuestPoint(questPoint: QuestPoint): Flow<Resource<QuestPoint>>
    fun updateQuestPoint(questPoint: QuestPoint): Flow<Resource<QuestPoint>>
    fun deleteQuestPoint(questPointId: String): Flow<Resource<Unit>>

    fun createQuest(quest: Quest): Flow<Resource<Quest>>
    fun updateQuest(quest: Quest): Flow<Resource<Quest>>
    fun deleteQuest(questId: String): Flow<Resource<Unit>>

    fun getAllReports(page: Int, size: Int): Flow<Resource<List<Report>>>
    fun getReportById(id: String): Flow<Resource<Report>>
    fun updateReport(report: Report): Flow<Resource<Report>>
    fun deleteReport(id: String): Flow<Resource<Unit>>

    fun getAllUsers(page: Int, size: Int): Flow<Resource<List<UserAccount>>>
    fun getUserById(id: String): Flow<Resource<UserAccount>>
    fun createUser(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String,
        role: UserRole,
        avatarFile: File? = null
    ): Flow<Resource<UserAccount>>

    fun updateUser(
        id: String,
        email: String,
        displayName: String,
        nativeLanguageId: String,
        role: UserRole,
        password: String? = null,
        avatarFile: File? = null
    ): Flow<Resource<UserAccount>>

    fun deleteUser(id: String): Flow<Resource<Unit>>

    fun getAllTransactions(page: Int, size: Int): Flow<Resource<List<TransactionRecord>>>

    fun getProducts(
        page: Int,
        size: Int,
        query: String? = null,
        type: TargetType? = null
    ): Flow<Resource<List<Product>>>
    fun createProduct(product: Product): Flow<Resource<Product>>
    fun deleteProduct(productId: String): Flow<Resource<Unit>>

    fun getAllCities(page: Int, size: Int): Flow<Resource<List<City>>>
    fun getAllQuests(page: Int, size: Int): Flow<Resource<List<Quest>>>
    fun getAllQuestPoints(page: Int, size: Int): Flow<Resource<List<QuestPoint>>>
    fun updateProduct(product: Product): Flow<Resource<Product>>

    // Adicione à interface AdminRepository
    fun getCharacters(query: String? = null): Flow<Resource<List<CharacterEntity>>>
    fun deleteCharacter(id: String): Flow<Resource<Unit>>
    fun saveCharacter(
        id: String?,
        name: String,
        avatarUrl: String,
        isAi: Boolean,
        voiceUrl: String?,
        persona: Persona? = null
    ): Flow<Resource<CharacterEntity>>

    fun getAchievements(query: String? = null): Flow<Resource<List<Achievement>>>
    fun deleteAchievement(id: String): Flow<Resource<Unit>>
    fun saveAchievement(
        id: String?,
        name: String,
        description: String,
        iconUrl: String,
        xpReward: Int,
        keyName: String,
        rarity: RarityType
    ): Flow<Resource<Achievement>>
}