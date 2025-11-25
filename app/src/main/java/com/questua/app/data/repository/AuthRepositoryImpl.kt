package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.core.network.TokenManager
import com.questua.app.data.remote.api.AuthApi
import com.questua.app.data.remote.dto.*
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository, SafeApiCall() {

    override fun login(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.login(LoginRequest(email, password)) }
        when (result) {
            is Resource.Success -> {
                result.data?.token?.let { token ->
                    tokenManager.saveToken(token)
                    emit(Resource.Success(token))
                } ?: emit(Resource.Error("Token vazio"))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun register(email: String, displayName: String, password: String, nativeLanguageId: String): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())
        val request = RegisterRequest(email, password, displayName, nativeLanguageId)
        val result = safeApiCall { api.register(request) }

        when (result) {
            is Resource.Success -> {
                result.data?.let { dto ->
                    val user = UserAccount(dto.id, dto.email, dto.displayName, dto.nativeLanguageId)
                    emit(Resource.Success(user))
                } ?: emit(Resource.Error("Resposta vazia"))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }
}