package com.questua.app.domain.model

import com.questua.app.domain.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val nativeLanguageId: String,
    val role: UserRole,
    val createdAt: String,
    val lastActiveAt: String? = null
)