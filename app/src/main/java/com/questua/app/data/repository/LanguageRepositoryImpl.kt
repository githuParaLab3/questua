package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.LanguageApi
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val api: LanguageApi
) : LanguageRepository, SafeApiCall() {

    override fun getAvailableLanguages(query: String?): Flow<Resource<List<Language>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.list(q = query) }

        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar idiomas"))
        }
    }

    override fun getLanguageById(id: String): Flow<Resource<Language>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.getById(id) }

        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar idioma"))
        }
    }
}