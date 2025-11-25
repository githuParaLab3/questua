package com.questua.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageResponse(
    val id: String,
    @SerialName("codeLanguage") val code: String, // O back deve retornar "codeLanguage"
    @SerialName("nameLanguage") val name: String, // O back deve retornar "nameLanguage"
    val iconUrl: String? = null
)