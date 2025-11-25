package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UnlockRequirement(
    val premiumAccess: Boolean = false,
    val requiredGamificationLevel: Int? = null,
    val requiredCefrLevel: String? = null,
    val requiredQuests: List<String> = emptyList(),
    val requiredQuestPoints: List<String> = emptyList()
)