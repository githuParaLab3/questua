package com.questua.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val nativeLanguageId: String
)