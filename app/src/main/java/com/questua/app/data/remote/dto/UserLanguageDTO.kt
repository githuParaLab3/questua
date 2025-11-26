package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.StatusLanguage
import com.questua.app.domain.model.UnlockedContent
import kotlinx.serialization.Serializable

@Serializable
data class UserLanguageRequestDTO(
    val userId: String,
    val languageId: String,
    val statusLanguage: StatusLanguage = StatusLanguage.ACTIVE,
    val cefrLevel: String = "A1",
    val gamificationLevel: Int = 1,
    val xpTotal: Int = 0,
    val streakDays: Int = 0,
    val unlockedContent: UnlockedContent? = null,
    val startedAt: String,
    val lastActiveAt: String? = null
)

@Serializable
data class UserLanguageResponseDTO(
    val id: String,
    val userId: String,
    val languageId: String,
    val statusLanguage: StatusLanguage,
    val cefrLevel: String,
    val gamificationLevel: Int,
    val xpTotal: Int,
    val streakDays: Int,
    val unlockedContent: UnlockedContent?,
    val startedAt: String,
    val lastActiveAt: String?
)