package com.questua.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageResponse(
    val id: String,
    @SerialName("codeLanguage") val code: String,
    @SerialName("nameLanguage") val name: String,
    val iconUrl: String? = null
)