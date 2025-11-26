package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserAchievementRequestDTO(
    val userId: String,
    val achievementId: String,
    val languageId: String? = null
)

@Serializable
data class UserAchievementResponseDTO(
    val id: String,
    val userId: String,
    val achievementId: String,
    val languageId: String?,
    val awardedAt: String
)