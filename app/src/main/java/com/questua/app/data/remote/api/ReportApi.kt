package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.ReportRequestDTO
import com.questua.app.data.remote.dto.ReportResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface ReportApi {
    @GET("reports")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageResponse<ReportResponseDTO>>

    @GET("reports/{id}")
    suspend fun getById(@Path("id") id: String): Response<ReportResponseDTO>

    @POST("reports")
    suspend fun create(@Body dto: ReportRequestDTO): Response<ReportResponseDTO>

    @PUT("reports/{id}")
    suspend fun update(@Path("id") id: String, @Body dto: ReportRequestDTO): Response<ReportResponseDTO>

    @DELETE("reports/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}