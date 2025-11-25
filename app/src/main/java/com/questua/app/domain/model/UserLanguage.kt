package com.questua.app.domain.model

import com.questua.app.domain.enums.StatusLanguage
import kotlinx.serialization.Serializable

@Serializable
data class UserLanguage(
    val id: String,
    val userId: String,
    val languageId: String,
    val status: StatusLanguage,
    val cefrLevel: String,
    val gamificationLevel: Int,
    val xpTotal: Int,
    val streakDays: Int,
    val unlockedContent: UnlockedContent? = null,
    val startedAt: String,
    val lastActiveAt: String? = null
)


@Serializable
data class UnlockedContent(
    val cities: List<String> = emptyList(),
    val questPoints: List<String> = emptyList(),
    val quests: List<String> = emptyList()
)