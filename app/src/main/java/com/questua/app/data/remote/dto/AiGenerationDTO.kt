package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class GeminiRequestDTO(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Double = 0.7,
    val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiResponseDTO(
    val candidates: List<GeminiCandidate>? = null,
    val usageMetadata: GeminiUsageMetadata? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null
)

@Serializable
data class GeminiUsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)


@Serializable
data class GenerateQuestPointRequestDTO(
    val cityId: String,
    val theme: String,
    val targetLanguage: String = "PT",
    val modelName: String = "gemini-2.0-flash"
)

@Serializable
data class GenerateQuestRequestDTO(
    val questPointId: String,
    val context: String,
    val difficultyLevel: Int = 1,
    val targetLanguage: String = "PT",
    val modelName: String = "gemini-2.0-flash"
)

@Serializable
data class GenerateCharacterRequestDTO(
    val archetype: String,
    val targetLanguage: String = "PT",
    val modelName: String = "gemini-2.0-flash"
)

@Serializable
data class GenerateDialogueRequestDTO(
    val questId: String? = null,
    val speakerCharacterId: String,
    val context: String,
    val inputMode: String = "CHOICE",
    val targetLanguage: String = "PT",
    val modelName: String = "gemini-2.0-flash"
)

@Serializable
data class GenerateAchievementRequestDTO(
    val triggerAction: String,
    val difficulty: String,
    val targetLanguage: String = "PT",
    val modelName: String = "gemini-2.0-flash"
)