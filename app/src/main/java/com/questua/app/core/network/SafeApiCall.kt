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
                    if (body != null) return@withContext Resource.Success(body)
                }
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Erro desconhecido: ${response.code()}")
            } catch (e: Exception) {
                when (e) {
                    is IOException -> Resource.Error("Sem conexão com a internet")
                    is HttpException -> Resource.Error("Erro no servidor: ${e.message}")
                    else -> Resource.Error(e.localizedMessage ?: "Erro inesperado")
                }
            }
        }
    }
}