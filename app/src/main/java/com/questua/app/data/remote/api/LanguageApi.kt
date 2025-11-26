package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.LanguageRequestDTO
import com.questua.app.data.remote.dto.LanguageResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.http.*

interface LanguageApi {

    @GET("api/languages")
    suspend fun list(
        @Query("q") q: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<LanguageResponseDTO>

    @GET("api/languages/{id}")
    suspend fun getById(@Path("id") id: String): LanguageResponseDTO

    @GET("api/languages/code/{code}")
    suspend fun getByCode(@Path("code") code: String): LanguageResponseDTO?

    @POST("api/languages")
    suspend fun create(@Body dto: LanguageRequestDTO): LanguageResponseDTO

    @PUT("api/languages/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: LanguageRequestDTO
    ): LanguageResponseDTO

    @DELETE("api/languages/{id}")
    suspend fun delete(@Path("id") id: String)
}