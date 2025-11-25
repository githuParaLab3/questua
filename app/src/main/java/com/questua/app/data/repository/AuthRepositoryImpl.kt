package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.core.network.TokenManager
import com.questua.app.data.remote.api.AuthApi
import com.questua.app.data.remote.dto.LoginRequest
import com.questua.app.data.remote.dto.RegisterRequest
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

        val request = LoginRequest(email = email, password = password)

        // O Backend retorna LoginResponseDTO { token: String }
        val result = safeApiCall { api.login(request) }

        when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response != null) {
                    // Salva o token
                    tokenManager.saveToken(response.token)
                    // OBS: Não temos o userId aqui. Ele terá que ser pego
                    // decodificando o JWT ou chamando um endpoint /users/me depois.

                    emit(Resource.Success(response.token))
                } else {
                    emit(Resource.Error("Resposta vazia do servidor"))
                }
            }
            is Resource.Error -> {
                emit(Resource.Error(result.message ?: "Erro desconhecido"))
            }
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun register(
        email: String,
        displayName: String,
        password: String,
        nativeLanguageId: String
    ): Flow<Resource<UserAccount>> = flow {
        emit(Resource.Loading())

        val request = RegisterRequest(
            email = email,
            password = password, // Backend espera "password", não "passwordUser"
            displayName = displayName,
            nativeLanguageId = nativeLanguageId,
            avatarUrl = null // Opcional
        )

        val result = safeApiCall { api.register(request) }

        when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response != null) {
                    // O registro retorna os dados do usuário, mas NÃO O TOKEN.
                    // O fluxo da UI deve ser: Registrar -> Sucesso -> Fazer Login Automático

                    val user = UserAccount(
                        id = response.id,
                        email = response.email,
                        displayName = response.displayName,
                        nativeLanguage = response.nativeLanguageId, // Backend retorna o ID
                        learningLanguage = null
                    )
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("Erro no registro"))
                }
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Falha ao registrar"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    override fun logout(): Flow<Resource<Unit>> = flow {
        tokenManager.clearData()
        emit(Resource.Success(Unit))
    }
}