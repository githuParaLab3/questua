package com.questua.app.domain.model

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val nativeLanguage: String? = null, // ID ou Código do idioma nativo
    val learningLanguage: String? = null, // ID ou Código do idioma que está aprendendo
    val xp: Int = 0,
    val level: Int = 1
)