package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.UserLanguageResponseDTO
import com.questua.app.domain.model.UserLanguage

fun UserLanguageResponseDTO.toDomain(): UserLanguage {
    return UserLanguage(
        id = this.id,
        userId = this.userId,
        languageId = this.languageId,
        status = this.statusLanguage,
        cefrLevel = this.cefrLevel,
        gamificationLevel = this.gamificationLevel,
        xpTotal = this.xpTotal,
        streakDays = this.streakDays,
        unlockedContent = this.unlockedContent,
        startedAt = this.startedAt,
        lastActiveAt = this.lastActiveAt
    )
}