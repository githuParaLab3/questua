package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.remote.api.LanguageApi
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val api: LanguageApi
) : LanguageRepository, SafeApiCall() {

    override fun getAvailableLanguages(): Flow<Resource<List<Language>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.getLanguages() }
        when (result) {
            is Resource.Success -> {
                val list = result.data?.map {
                    Language(it.id, it.code, it.name, it.iconUrl)
                } ?: emptyList()
                emit(Resource.Success(list))
            }
            is Resource.Error -> emit(Resource.Error(result.message ?: "Erro"))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }
}