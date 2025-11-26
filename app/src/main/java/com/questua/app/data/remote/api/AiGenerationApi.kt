package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AiGenerationApi {

    @POST("api/ai/quest-point")
    suspend fun generateQuestPoint(@Body dto: GenerateQuestPointRequestDTO): QuestPointResponseDTO

    @POST("api/ai/quest")
    suspend fun generateQuest(@Body dto: GenerateQuestRequestDTO): QuestResponseDTO

    @POST("api/ai/character")
    suspend fun generateCharacter(@Body dto: GenerateCharacterRequestDTO): CharacterEntityResponseDTO

    @POST("api/ai/dialogue")
    suspend fun generateDialogue(@Body dto: GenerateDialogueRequestDTO): SceneDialogueResponseDTO

    @POST("api/ai/achievement")
    suspend fun generateAchievement(@Body dto: GenerateAchievementRequestDTO): AchievementResponseDTO
}