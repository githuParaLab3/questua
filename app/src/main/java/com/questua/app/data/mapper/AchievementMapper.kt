package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.AchievementResponseDTO
import com.questua.app.domain.model.Achievement

fun AchievementResponseDTO.toDomain(): Achievement {
    return Achievement(
        id = this.id,
        keyName = this.keyName,
        name = this.nameAchievement,
        description = this.descriptionAchievement,
        iconUrl = this.iconUrl,
        rarity = this.rarity,
        xpReward = this.xpReward,
        metadata = this.metadata,
        createdAt = this.createdAt
    )
}