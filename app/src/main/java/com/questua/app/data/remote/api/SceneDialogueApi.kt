package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.SceneDialogueRequestDTO
import com.questua.app.data.remote.dto.SceneDialogueResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface SceneDialogueApi {
    @GET("scene-dialogues")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<SceneDialogueResponseDTO>>

    @GET("scene-dialogues/{id}")
    suspend fun getById(@Path("id") id: String): Response<SceneDialogueResponseDTO>

    @POST("scene-dialogues")
    suspend fun create(@Body dto: SceneDialogueRequestDTO): Response<SceneDialogueResponseDTO>

    @PUT("scene-dialogues/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: SceneDialogueRequestDTO): Response<SceneDialogueResponseDTO>

    @DELETE("scene-dialogues/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}