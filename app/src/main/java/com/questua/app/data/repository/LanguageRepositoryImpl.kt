package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.LanguageApi
import com.questua.app.data.remote.api.UploadApi
import com.questua.app.data.remote.dto.LanguageRequestDTO
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val api: LanguageApi,
    private val uploadApi: UploadApi
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

    override fun createLanguage(name: String, code: String, iconFile: File?): Flow<Resource<Language>> = flow {
        emit(Resource.Loading())
        var iconUrl: String? = null

        if (iconFile != null) {
            val requestFile = iconFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", iconFile.name, requestFile)
            val uploadResult = safeApiCall { uploadApi.uploadArchive(body, "languages") }
            if (uploadResult is Resource.Success) {
                iconUrl = uploadResult.data?.get("url")
            }
        }

        val result = safeApiCall { api.create(LanguageRequestDTO(code, name, iconUrl)) }
        emit(when (result) {
            is Resource.Success -> Resource.Success(result.data!!.toDomain())
            is Resource.Error -> Resource.Error(result.message ?: "Erro ao criar idioma")
            is Resource.Loading -> Resource.Loading()
        })
    }

    override fun updateLanguage(id: String, name: String, code: String, iconFile: File?): Flow<Resource<Language>> = flow {
        emit(Resource.Loading())

        val currentLanguage = safeApiCall { api.getById(id) }
        var iconUrl: String? = if (currentLanguage is Resource.Success) currentLanguage.data?.iconUrl else null

        if (iconFile != null) {
            val requestFile = iconFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", iconFile.name, requestFile)
            val uploadResult = safeApiCall { uploadApi.uploadArchive(body, "languages") }
            if (uploadResult is Resource.Success) {
                iconUrl = uploadResult.data?.get("url")
            }
        }

        val result = safeApiCall { api.update(id, LanguageRequestDTO(code, name, iconUrl)) }
        emit(when (result) {
            is Resource.Success -> Resource.Success(result.data!!.toDomain())
            is Resource.Error -> Resource.Error(result.message ?: "Erro ao atualizar idioma")
            is Resource.Loading -> Resource.Loading()
        })
    }

    override fun deleteLanguage(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { api.delete(id) }
        emit(when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message ?: "Erro ao excluir idioma")
            is Resource.Loading -> Resource.Loading()
        })
    }
}