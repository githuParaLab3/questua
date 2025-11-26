package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.AiGenerationLogRequestDTO
import com.questua.app.data.remote.dto.AiGenerationLogResponseDTO
import com.questua.app.data.remote.dto.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface AiGenerationLogApi {
    @GET("api/ai-generation-logs")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<AiGenerationLogResponseDTO>>

    @GET("api/ai-generation-logs/{id}")
    suspend fun getById(@Path("id") id: String): Response<AiGenerationLogResponseDTO>

    @POST("api/ai-generation-logs")
    suspend fun create(@Body dto: AiGenerationLogRequestDTO): Response<AiGenerationLogResponseDTO>

    @PUT("api/ai-generation-logs/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: AiGenerationLogRequestDTO): Response<AiGenerationLogResponseDTO>

    @DELETE("api/ai-generation-logs/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}