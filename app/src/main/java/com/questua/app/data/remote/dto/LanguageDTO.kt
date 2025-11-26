package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LanguageRequestDTO(
    val codeLanguage: String,
    val nameLanguage: String,
    val iconUrl: String? = null
)

@Serializable
data class LanguageResponseDTO(
    val id: String,
    val codeLanguage: String,
    val nameLanguage: String,
    val iconUrl: String?
)