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
    fun generateDialogue(
        speakerId: String,
        context: String,
        questId: String?,
        inputMode: String
    ): Flow<Resource<SceneDialogue>>
    fun generateAchievement(trigger: String, difficulty: String): Flow<Resource<Achievement>>

    fun getAiLogs(page: Int, size: Int): Flow<Resource<List<AiGenerationLog>>>
    fun getCities(query: String? = null): Flow<Resource<List<City>>>
    fun deleteCity(id: String): Flow<Resource<Unit>>

    fun uploadFile(file: java.io.File, folder: String): kotlinx.coroutines.flow.Flow<com.questua.app.core.common.Resource<String>>
    fun saveCity(
        id: String?,
        cityName: String,
        countryCode: String,
        descriptionCity: String,
        languageId: String,
        boundingPolygon: BoundingPolygon?,
        lat: Double,
        lon: Double,
        imageUrl: String?,
        iconUrl: String?,
        isPremium: Boolean,
        unlockRequirement: UnlockRequirement?,
        isAiGenerated: Boolean,
        isPublished: Boolean
    ): Flow<Resource<City>>

    fun getQuestPoints(query: String? = null): Flow<Resource<List<QuestPoint>>>
    fun deleteQuestPoint(id: String): Flow<Resource<Unit>>
    fun saveQuestPoint(
        id: String?,
        cityId: String,
        title: String,
        description: String,
        difficulty: Int,
        lat: Double,
        lon: Double,
        imageUrl: String?,
        iconUrl: String?,
        unlockRequirement: UnlockRequirement?,
        isPremium: Boolean,
        isAiGenerated: Boolean,
        isPublished: Boolean
    ): Flow<Resource<QuestPoint>>

    fun getQuests(query: String? = null): Flow<Resource<List<Quest>>>
    fun deleteQuest(id: String): Flow<Resource<Unit>>

    fun saveQuest(
        id: String?,
        questPointId: String,
        firstDialogueId: String?,
        title: String,
        description: String,
        difficulty: Int,
        orderIndex: Int,
        xpValue: Int,
        unlockRequirement: UnlockRequirement?,
        learningFocus: LearningFocus?,
        isPremium: Boolean,
        isAiGenerated: Boolean,
        isPublished: Boolean
    ): Flow<Resource<Quest>>

    fun getAllReports(page: Int, size: Int): Flow<Resource<List<Report>>>
    fun getReportById(id: String): Flow<Resource<Report>>
    fun updateReport(report: Report): Flow<Resource<Report>>
    fun deleteReport(id: String): Flow<Resource<Unit>>
    fun getAllUsers(page: Int, size: Int): Flow<Resource<List<UserAccount>>>
    fun getUserById(id: String): Flow<Resource<UserAccount>>
    fun createUser(email: String, displayName: String, password: String, nativeLanguageId: String, role: UserRole, avatarFile: File? = null): Flow<Resource<UserAccount>>
    fun updateUser(id: String, email: String, displayName: String, nativeLanguageId: String, role: UserRole, password: String? = null, avatarFile: File? = null): Flow<Resource<UserAccount>>
    fun deleteUser(id: String): Flow<Resource<Unit>>
    fun getAllTransactions(page: Int, size: Int): Flow<Resource<List<TransactionRecord>>>
    fun getProducts(page: Int, size: Int, query: String? = null, type: TargetType? = null): Flow<Resource<List<Product>>>
    fun createProduct(product: Product): Flow<Resource<Product>>
    fun deleteProduct(productId: String): Flow<Resource<Unit>>
    fun getAllCities(page: Int, size: Int): Flow<Resource<List<City>>>
    fun getAllQuests(page: Int, size: Int): Flow<Resource<List<Quest>>>
    fun getAllQuestPoints(page: Int, size: Int): Flow<Resource<List<QuestPoint>>>
    fun updateProduct(product: Product): Flow<Resource<Product>>
    fun getCharacters(query: String? = null): Flow<Resource<List<CharacterEntity>>>
    fun deleteCharacter(id: String): Flow<Resource<Unit>>
    fun saveCharacter(id: String?, name: String, avatarUrl: String, isAi: Boolean, voiceUrl: String?, persona: Persona? = null): Flow<Resource<CharacterEntity>>
    fun getAchievements(query: String? = null): Flow<Resource<List<Achievement>>>
    fun deleteAchievement(id: String): Flow<Resource<Unit>>
    fun saveAchievement(id: String?, name: String, description: String, iconUrl: String, xpReward: Int, keyName: String, rarity: RarityType): Flow<Resource<Achievement>>
    fun getDialogues(query: String? = null): Flow<Resource<List<SceneDialogue>>>
    fun deleteDialogue(id: String): Flow<Resource<Unit>>
    fun saveDialogue(id: String?, textContent: String, description: String, backgroundUrl: String, speakerCharacterId: String?, expectsUserResponse: Boolean, inputMode: com.questua.app.domain.enums.InputMode, nextDialogueId: String?): Flow<Resource<SceneDialogue>>
}