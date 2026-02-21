package com.questua.app.domain.model

import com.questua.app.domain.enums.AchievementConditionType
import com.questua.app.domain.enums.RarityType
import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String = "",
    val keyName: String? = null,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val xpReward: Int = 0,
    val rarity: RarityType = RarityType.COMMON,
    val isHidden: Boolean = false,
    val category: String? = null,
    val isGlobal: Boolean = true,
    val languageId: String? = null,
    val conditionType: AchievementConditionType = AchievementConditionType.COMPLETE_SPECIFIC_QUEST,
    val targetId: String? = null,
    val requiredAmount: Int = 1,
    val metadata: AchievementMetadata? = null,
    val createdAt: String? = null
)
@Serializable
data class AchievementMetadata(
    val category: String? = null,
    val descriptionExtra: String? = null
)