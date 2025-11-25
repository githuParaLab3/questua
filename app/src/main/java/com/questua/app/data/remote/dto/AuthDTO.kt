package com.questua.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val nativeLanguageId: String
)

@Serializable
data class RegisterResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val nativeLanguageId: String
)