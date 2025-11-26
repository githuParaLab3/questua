package com.questua.app.data.remote.dto

import com.questua.app.domain.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserAccountRequestDTO(
    val email: String,
    val displayName: String,
    val password: String?,
    val avatarUrl: String? = null,
    val nativeLanguageId: String,
    val userRole: UserRole = UserRole.USER
)

@Serializable
data class UserAccountResponseDTO(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val nativeLanguageId: String,
    val userRole: UserRole,
    val createdAt: String,
    val lastActiveAt: String?
)