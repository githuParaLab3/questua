package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserQuestApi {
    @GET("/user-quests")
    suspend fun getAll(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserQuestResponseDTO>>

    @GET("/user-quests/{id}")
    suspend fun getById(@Path("id") id: String): Response<UserQuestResponseDTO>

    @GET("/user-quests/user/{userId}")
    suspend fun getByUser(
        @Path("userId") userId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserQuestResponseDTO>>

    @GET("user-quests/quest/{questId}")
    suspend fun getByQuest(
        @Path("questId") questId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<UserQuestResponseDTO>>

    @POST("user-quests")
    suspend fun create(@Body dto: UserQuestRequestDTO): Response<UserQuestResponseDTO>

    @PUT("user-quests/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: UserQuestRequestDTO): Response<UserQuestResponseDTO>

    @DELETE("user-quests/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @POST("user-quests/{id}/submit")
    suspend fun submitResponse(
        @Path("id") id: String,
        @Body request: SubmitResponseRequestDTO
    ): Response<SubmitResponseResultDTO>
}