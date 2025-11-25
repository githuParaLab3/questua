package com.questua.app.domain.model

import com.questua.app.domain.enums.RarityType
import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val keyName: String,
    val name: String, // name_achievement
    val description: String? = null,
    val iconUrl: String? = null,
    val rarity: RarityType,
    val xpReward: Int,
    val metadata: AchievementMetadata? = null, // JSONB
    val createdAt: String
)
@Serializable
data class AchievementMetadata(
    val category: String? = null,
    val descriptionExtra: String? = null
)