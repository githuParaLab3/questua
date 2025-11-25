package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: String,
    val code: String, // ex: "en", "pt-br"
    val name: String, // ex: "Inglês", "Português"
    val iconUrl: String? = null
)