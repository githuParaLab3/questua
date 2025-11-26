package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.LoginRequestDTO
import com.questua.app.data.remote.dto.LoginResponseDTO
import com.questua.app.data.remote.dto.RegisterRequestDTO
import com.questua.app.data.remote.dto.RegisterResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body dto: LoginRequestDTO): Response<LoginResponseDTO>

    @POST("api/auth/register")
    suspend fun register(@Body dto: RegisterRequestDTO): Response<RegisterResponseDTO>
}