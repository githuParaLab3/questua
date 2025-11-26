package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.UserAchievementResponseDTO
import com.questua.app.domain.model.UserAchievement

fun UserAchievementResponseDTO.toDomain(): UserAchievement {
    return UserAchievement(
        id = this.id,
        userId = this.userId,
        achievementId = this.achievementId,
        languageId = this.languageId,
        awardedAt = this.awardedAt
    )
}