package com.questua.app.data.mapper

import com.questua.app.data.remote.dto.LanguageResponseDTO
import com.questua.app.domain.model.Language

fun LanguageResponseDTO.toDomain(): Language {
    return Language(
        id = this.id,
        code = this.codeLanguage,
        name = this.nameLanguage,
        iconUrl = this.iconUrl
    )
}