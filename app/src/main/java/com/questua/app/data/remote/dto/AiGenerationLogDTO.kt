package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.AiGenerationStatus
import com.questua.app.domain.enums.AiTargetType
import com.questua.app.domain.model.AiGenerationResponseMeta
import kotlinx.serialization.Serializable

@Serializable
data class AiGenerationLogRequestDTO(
    val userId: String? = null,
    val targetType: AiTargetType,
    val targetId: String? = null,
    val prompt: String,
    val modelName: String,
    val responseText: String? = null,
    val responseMeta: AiGenerationResponseMeta? = null,
    val statusGeneration: AiGenerationStatus
)

@Serializable
data class AiGenerationLogResponseDTO(
    val id: String,
    val userId: String? = null,
    val targetType: AiTargetType,
    val targetId: String? = null,
    val prompt: String,
    val modelName: String,
    val responseText: String? = null,
    val responseMeta: AiGenerationResponseMeta? = null,
    val statusGeneration: AiGenerationStatus,
    val createdAt: String
)