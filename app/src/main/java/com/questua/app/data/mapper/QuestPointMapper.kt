package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.QuestPointResponseDTO
import com.questua.app.domain.model.QuestPoint

fun QuestPointResponseDTO.toDomain(): QuestPoint {
    return QuestPoint(
        id = this.id,
        cityId = this.cityId,
        title = this.title,
        description = this.descriptionQpoint,
        difficulty = this.difficulty.toInt(),
        lat = this.lat ?: 0.0,
        lon = this.lon ?: 0.0,
        imageUrl = this.imageUrl,
        iconUrl = this.iconUrl,
        unlockRequirement = this.unlockRequirement,
        isPremium = this.isPremium,
        isAiGenerated = this.isAiGenerated,
        isPublished = this.isPublished,
        createdAt = this.createdAt
    )
}