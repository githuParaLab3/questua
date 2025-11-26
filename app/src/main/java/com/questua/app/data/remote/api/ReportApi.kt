package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.PageResponse
import com.questua.app.data.remote.dto.ReportRequestDTO
import com.questua.app.data.remote.dto.ReportResponseDTO
import retrofit2.http.*

interface ReportApi {

    @GET("api/reports")
    suspend fun list(
        @QueryMap filter: Map<String, String> = emptyMap(),
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): PageResponse<ReportResponseDTO>

    @GET("api/reports/{id}")
    suspend fun getById(@Path("id") id: String): ReportResponseDTO

    @POST("api/reports")
    suspend fun create(@Body dto: ReportRequestDTO): ReportResponseDTO

    @PUT("api/reports/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: ReportRequestDTO
    ): ReportResponseDTO

    @DELETE("api/reports/{id}")
    suspend fun delete(@Path("id") id: String)
}