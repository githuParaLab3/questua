package com.questua.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDTO(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponseDTO(
    val token: String
)

@Serializable
data class RegisterRequestDTO(
    val email: String,
    val password: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val nativeLanguageId: String
)

@Serializable
data class RegisterResponseDTO(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val nativeLanguageId: String
)