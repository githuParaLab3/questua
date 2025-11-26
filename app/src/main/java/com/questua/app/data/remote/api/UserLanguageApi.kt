package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.UserLanguageRequestDTO
import com.questua.app.data.remote.dto.UserLanguageResponseDTO
import retrofit2.http.*

interface UserLanguageApi {

    @GET("api/user-languages")
    suspend fun list(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserLanguageResponseDTO>

    @GET("api/user-languages/{id}")
    suspend fun getById(@Path("id") id: String): UserLanguageResponseDTO

    @GET("api/user-languages/user/{userId}")
    suspend fun getByUserId(
        @Path("userId") userId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserLanguageResponseDTO>

    @GET("api/user-languages/language/{languageId}")
    suspend fun getByLanguageId(
        @Path("languageId") languageId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserLanguageResponseDTO>

    @POST("api/user-languages")
    suspend fun create(@Body dto: UserLanguageRequestDTO): UserLanguageResponseDTO

    @PUT("api/user-languages/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: UserLanguageRequestDTO
    ): UserLanguageResponseDTO

    @DELETE("api/user-languages/{id}")
    suspend fun delete(@Path("id") id: String)
}