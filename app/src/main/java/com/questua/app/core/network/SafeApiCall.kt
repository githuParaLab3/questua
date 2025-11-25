package com.questua.app.core.network

import com.questua.app.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

abstract class SafeApiCall {
    suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        return@withContext Resource.Success(body)
                    }
                }
                // Tenta pegar a mensagem de erro do corpo da resposta, se houver
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    errorBody
                } else {
                    "Erro ${response.code()}: ${response.message()}"
                }
                return@withContext Resource.Error(errorMessage)

            } catch (e: Exception) {
                return@withContext when (e) {
                    is IOException -> Resource.Error("Sem conexão com a internet. Verifique sua rede.")
                    is HttpException -> Resource.Error("Erro no servidor: ${e.message()}")
                    else -> Resource.Error("Erro inesperado: ${e.localizedMessage}")
                }
            }
        }
    }
}