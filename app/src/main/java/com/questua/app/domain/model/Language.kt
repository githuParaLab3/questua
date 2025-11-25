package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: String,
    val code: String,
    val name: String,
    val iconUrl: String? = null
)