package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Login agora retorna apenas o Token (String), pois é o que o back manda
    fun login(email: String, password: String): Flow<Resource<String>>

    // Register retorna o Usuário criado
    fun register(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String // ID do idioma é obrigatório
    ): Flow<Resource<UserAccount>>

    fun logout(): Flow<Resource<Unit>>
}