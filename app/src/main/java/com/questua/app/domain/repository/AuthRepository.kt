package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<String>>
    fun register(email: String, displayName: String, password: String, nativeLanguageId: String): Flow<Resource<UserAccount>>
}