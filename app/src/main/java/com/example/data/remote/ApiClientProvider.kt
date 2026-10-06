package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiClientProvider private constructor() {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    private var currentBaseUrl: String = "https://www.sankavollerei.web.id"
    private var apiService: AnimeApiService = createService(currentBaseUrl)

    @Synchronized
    fun getApiService(): AnimeApiService {
        return apiService
    }

    @Synchronized
    fun updateBaseUrl(newBaseUrl: String) {
        val sanitized = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
        if (sanitized != currentBaseUrl) {
            currentBaseUrl = sanitized
            apiService = createService(sanitized)
        }
    }

    private fun createService(baseUrl: String): AnimeApiService {
        val sanitized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(sanitized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(AnimeApiService::class.java)
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiClientProvider? = null

        fun getInstance(): ApiClientProvider {
            return INSTANCE ?: synchronized(this) {
                val instance = ApiClientProvider()
                INSTANCE = instance
                instance
            }
        }
    }
}
