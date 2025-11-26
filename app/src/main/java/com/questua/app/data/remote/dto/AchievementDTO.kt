package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.AchievementMetadata
import kotlinx.serialization.Serializable

@Serializable
data class AchievementRequestDTO(
    val keyName: String,
    val nameAchievement: String,
    val descriptionAchievement: String? = null,
    val iconUrl: String? = null,
    val rarity: RarityType,
    val xpReward: Int,
    val metadata: AchievementMetadata? = null
)

@Serializable
data class AchievementResponseDTO(
    val id: String,
    val keyName: String,
    val nameAchievement: String,
    val descriptionAchievement: String?,
    val iconUrl: String?,
    val rarity: RarityType,
    val xpReward: Int,
    val metadata: AchievementMetadata?,
    val createdAt: String
)