package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.LanguageResponse
import retrofit2.Response
import retrofit2.http.GET

interface LanguageApi {
    @GET("languages") // Verifique se seu controller mapeia para /api/languages ou apenas /languages
    suspend fun getLanguages(): Response<List<LanguageResponse>>
}