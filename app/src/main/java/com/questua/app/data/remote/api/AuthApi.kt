package com.questua.app.data.remote.api

import com.questua.app.data.remote.dto.LoginRequest
import com.questua.app.data.remote.dto.LoginResponse
import com.questua.app.data.remote.dto.RegisterRequest
import com.questua.app.data.remote.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}