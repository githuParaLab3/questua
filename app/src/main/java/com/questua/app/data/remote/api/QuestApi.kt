package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.QuestRequestDTO
import com.questua.app.data.remote.dto.QuestResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface QuestApi {
    @GET("quests")
    suspend fun getAll(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<QuestResponseDTO>>

    @GET("quests/{id}")
    suspend fun getById(@Path("id") id: String): Response<QuestResponseDTO>

    @GET("quests/point/{questPointId}")
    suspend fun getByQuestPoint(
        @Path("questPointId") questPointId: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<QuestResponseDTO>>

    @POST("quests")
    suspend fun create(@Body dto: QuestRequestDTO): Response<QuestResponseDTO>

    @PUT("quests/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: QuestRequestDTO): Response<QuestResponseDTO>

    @DELETE("quests/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @PUT("quests/{id}/sync-xp")
    suspend fun syncXp(@Path("id") id: String): Response<QuestResponseDTO>
}