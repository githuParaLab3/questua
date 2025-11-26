package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.core.network.TokenManager
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.AuthApi
import com.questua.app.data.remote.dto.LoginRequestDTO
import com.questua.app.data.remote.dto.RegisterRequestDTO
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository, SafeApiCall() {

    override fun login(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall {
            api.login(LoginRequestDTO(email, password))
        }

        when (result) {
            is Resource.Success -> {
                val token = result.data!!.token
                tokenManager.saveToken(token)
                emit(Resource.Success(token))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Login falhou"))
            else -> Unit
        }
    }

    override fun register(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String
    ): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())
        val request = RegisterRequestDTO(email, password, displayName, null, nativeLanguageId)
        val result = safeApiCall { api.register(request) }

        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data!!.toDomain()))
            is Resource.Error -> emit(Resource.Error(result.message ?: "Registro falhou"))
            else -> Unit
        }
    }

    override fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        tokenManager.clearData() // CORRIGIDO: clearToken -> clearData
        emit(Resource.Success(Unit))
    }


}