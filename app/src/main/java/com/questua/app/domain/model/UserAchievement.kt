package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAchievement(
    val id: String,
    val userId: String,
    val achievementId: String,
    val languageId: String? = null,
    val awardedAt: String
)