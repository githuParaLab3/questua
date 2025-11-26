package com.questua.app.core.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (url.contains("/auth/login") || url.contains("/auth/register")) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenManager.token.first() }
        val newRequest = request.newBuilder()

        if (!token.isNullOrEmpty()) {
            newRequest.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(newRequest.build())
    }
}