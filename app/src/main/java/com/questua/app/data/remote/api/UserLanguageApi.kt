package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.UserLanguageRequestDTO
import com.questua.app.data.remote.dto.UserLanguageResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface UserLanguageApi {
    @GET("/user-languages")
    suspend fun list(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserLanguageResponseDTO>>

    @GET("/user-languages/{id}")
    suspend fun getById(@Path("id") id: String): Response<UserLanguageResponseDTO>

    @GET("user-languages/user/{userId}")
    suspend fun getByUserId(
        @Path("userId") userId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserLanguageResponseDTO>>

    @GET("user-languages/language/{languageId}")
    suspend fun getByLanguageId(
        @Path("languageId") languageId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserLanguageResponseDTO>>

    @POST("user-languages")
    suspend fun create(@Body dto: UserLanguageRequestDTO): Response<UserLanguageResponseDTO>

    @PUT("user-languages/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UserLanguageRequestDTO): Response<UserLanguageResponseDTO>

    @DELETE("user-languages/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @POST("user-languages/{languageId}/sync-progress")
    suspend fun syncProgress(@Path("languageId") languageId: String): Response<Void>
}