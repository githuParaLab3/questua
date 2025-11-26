package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.AiGenerationLogResponseDTO
import com.questua.app.domain.model.AiGenerationLog

fun AiGenerationLogResponseDTO.toDomain(): AiGenerationLog {
    return AiGenerationLog(
        id = this.id,
        userId = this.userId,
        targetType = this.targetType,
        targetId = this.targetId,
        prompt = this.prompt,
        modelName = this.modelName,
        responseText = this.responseText,
        responseMeta = this.responseMeta,
        status = this.statusGeneration,
        createdAt = this.createdAt
    )
}