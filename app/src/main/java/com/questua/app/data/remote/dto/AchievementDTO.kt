package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.AchievementConditionType
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.AchievementMetadata
import kotlinx.serialization.Serializable

@Serializable
data class AchievementRequestDTO(
    val keyName: String? = null,
    val nameAchievement: String,
    val descriptionAchievement: String,
    val iconUrl: String? = null,
    val xpReward: Int = 0,
    val rarity: RarityType,
    val isHidden: Boolean = false,
    val category: String? = null,
    val isGlobal: Boolean = true,
    val languageId: String? = null,
    val conditionType: AchievementConditionType,
    val targetId: String? = null,
    val requiredAmount: Int = 1,
    val metadata: AchievementMetadata? = null
)

@Serializable
data class AchievementResponseDTO(
    val id: String,
    val keyName: String? = null,
    val nameAchievement: String,
    val descriptionAchievement: String,
    val iconUrl: String? = null,
    val xpReward: Int,
    val rarity: RarityType,
    val isHidden: Boolean,
    val category: String?,
    val isGlobal: Boolean,
    val languageId: String?,
    val conditionType: AchievementConditionType,
    val targetId: String?,
    val requiredAmount: Int,
    val metadata: AchievementMetadata?,
    val createdAt: String? = null
)