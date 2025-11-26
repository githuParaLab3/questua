package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiGenerationApi {
    @POST("ai/quest-point")
    suspend fun generateQuestPoint(@Body dto: GenerateQuestPointRequestDTO): Response<QuestPointResponseDTO>

    @POST("ai/quest")
    suspend fun generateQuest(@Body dto: GenerateQuestRequestDTO): Response<QuestResponseDTO>

    @POST("ai/character")
    suspend fun generateCharacter(@Body dto: GenerateCharacterRequestDTO): Response<CharacterEntityResponseDTO>

    @POST("ai/dialogue")
    suspend fun generateDialogue(@Body dto: GenerateDialogueRequestDTO): Response<SceneDialogueResponseDTO>

    @POST("ai/achievement")
    suspend fun generateAchievement(@Body dto: GenerateAchievementRequestDTO): Response<AchievementResponseDTO>
}