package com.example.nasacosmosmessenger.data.remote.interceptor

import com.example.nasacosmosmessenger.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that automatically adds NASA API key to all requests.
 *
 * This interceptor appends the API key as a query parameter to every outgoing request,
 * eliminating the need to manually add it in each API call.
 */
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.NASA_API_KEY)
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}
