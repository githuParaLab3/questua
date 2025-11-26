package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.TransactionRecordRequestDTO
import com.questua.app.data.remote.dto.TransactionRecordResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface TransactionRecordApi {
    @GET("api/transactions")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<TransactionRecordResponseDTO>>

    @GET("api/transactions/{id}")
    suspend fun getById(@Path("id") id: String): Response<TransactionRecordResponseDTO>

    @POST("api/transactions")
    suspend fun create(@Body dto: TransactionRecordRequestDTO): Response<TransactionRecordResponseDTO>

    @PUT("api/transactions/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: TransactionRecordRequestDTO): Response<TransactionRecordResponseDTO>

    @DELETE("api/transactions/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}