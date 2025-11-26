package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.LanguageRequestDTO
import com.questua.app.data.remote.dto.LanguageResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface LanguageApi {
    @GET("languages")
    suspend fun list(
        @Query("q") q: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<LanguageResponseDTO>>

    @GET("languages/{id}")
    suspend fun getById(@Path("id") id: String): Response<LanguageResponseDTO>

    @GET("languages/code/{code}")
    suspend fun getByCode(@Path("code") code: String): Response<LanguageResponseDTO?>

    @POST("languages")
    suspend fun create(@Body dto: LanguageRequestDTO): Response<LanguageResponseDTO>

    @PUT("languages/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: LanguageRequestDTO): Response<LanguageResponseDTO>

    @DELETE("languages/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}