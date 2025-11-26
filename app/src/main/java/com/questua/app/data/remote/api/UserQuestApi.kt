package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.*
import retrofit2.http.*

interface UserQuestApi {

    @GET("api/user-quests")
    suspend fun getAll(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserQuestResponseDTO>

    @GET("api/user-quests/{id}")
    suspend fun getById(@Path("id") id: String): UserQuestResponseDTO

    @GET("api/user-quests/user/{userId}")
    suspend fun getByUser(
        @Path("userId") userId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserQuestResponseDTO>

    @GET("api/user-quests/quest/{questId}")
    suspend fun getByQuest(
        @Path("questId") questId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<UserQuestResponseDTO>

    @POST("api/user-quests")
    suspend fun create(@Body dto: UserQuestRequestDTO): UserQuestResponseDTO

    @PUT("api/user-quests/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: UserQuestRequestDTO
    ): UserQuestResponseDTO

    @DELETE("api/user-quests/{id}")
    suspend fun delete(@Path("id") id: String)

    @POST("api/user-quests/{id}/submit")
    suspend fun submitResponse(
        @Path("id") id: String,
        @Body request: SubmitResponseRequestDTO
    ): SubmitResponseResultDTO
}