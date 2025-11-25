package com.questua.app.domain.model

import com.questua.app.domain.enums.AiGenerationStatus
import com.questua.app.domain.enums.AiTargetType
import kotlinx.serialization.Serializable

@Serializable
data class AiGenerationLog(
    val id: String,
    val userId: String? = null,
    val targetType: AiTargetType,
    val targetId: String? = null,
    val prompt: String,
    val modelName: String,
    val responseText: String? = null,
    val responseMeta: AiGenerationResponseMeta? = null,
    val status: AiGenerationStatus,
    val createdAt: String
)

@Serializable
data class AiGenerationResponseMeta(
    val tokensUsed: Int?,
    val durationSeconds: Double?,
    val latencyMs: Long? = null,
    val temperature: Double? = null,
    val reasoning: String? = null,
    val extraInfo: String?
)