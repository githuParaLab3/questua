package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, passwordUser: String): Flow<Resource<String>> // Retorna Token
    fun register(email: String, displayName: String, passwordUser: String, nativeLanguageId: String): Flow<Resource<UserAccount>>
    fun logout(): Flow<Resource<Unit>>
}