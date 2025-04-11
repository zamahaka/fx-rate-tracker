package com.example.fxratetracker.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
    private val host: String,
    private val apiKeyName: String,
    private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        if (url.host != host) return chain.proceed(request)
        if (url.queryParameter(apiKeyName) != null) return chain.proceed(request)

        val urlWithApiKey = url.newBuilder()
            .addQueryParameter(apiKeyName, apiKey)
            .build()

        return chain.proceed(request.newBuilder().url(urlWithApiKey).build())
    }
}