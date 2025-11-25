package com.questua.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- LOGIN (Espelha LoginRequestDTO e LoginResponseDTO do Backend) ---

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String // O Backend só retorna isso!
)

// --- REGISTER (Espelha RegisterRequestDTO e RegisterResponseDTO do Backend) ---

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerialName("displayName") val displayName: String, // Nome exato do JSON
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("nativeLanguageId") val nativeLanguageId: String // Obrigatório no seu Backend
)

@Serializable
data class RegisterResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val nativeLanguageId: String
)