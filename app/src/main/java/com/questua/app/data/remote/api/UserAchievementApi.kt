package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.UserAchievementRequestDTO
import com.questua.app.data.remote.dto.UserAchievementResponseDTO
import retrofit2.http.*

interface UserAchievementApi {

    @GET("api/user-achievements")
    suspend fun list(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserAchievementResponseDTO>

    @GET("api/user-achievements/{id}")
    suspend fun getById(@Path("id") id: String): UserAchievementResponseDTO

    @GET("api/user-achievements/user/{userId}")
    suspend fun listByUser(
        @Path("userId") userId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserAchievementResponseDTO>

    @GET("api/user-achievements/user/{userId}/language/{languageId}")
    suspend fun listByUserAndLanguage(
        @Path("userId") userId: String,
        @Path("languageId") languageId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserAchievementResponseDTO>

    @POST("api/user-achievements")
    suspend fun create(@Body dto: UserAchievementRequestDTO): UserAchievementResponseDTO

    @PUT("api/user-achievements/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: UserAchievementRequestDTO
    ): UserAchievementResponseDTO

    @DELETE("api/user-achievements/{id}")
    suspend fun delete(@Path("id") id: String)
}