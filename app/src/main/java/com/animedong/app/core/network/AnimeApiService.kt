package com.animedong.app.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.animedong.app.data.dto.*
import com.google.gson.JsonElement

object AppConstants {
    const val DEFAULT_BASE_URL = "https://www.sankavollerei.web.id/"
    const val USER_AGENT = "AnimeDongApp/1.0"
}

/** baseUrl bisa diganti via Remote Config tanpa rilis update. */
fun createRetrofit(baseUrl: String = AppConstants.DEFAULT_BASE_URL): Retrofit {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", AppConstants.USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
            )
        }
        .addInterceptor(logging)
        .build()

    val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return Retrofit.Builder()
        .baseUrl(normalized)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

/**
 * Satu pintu ke semua endpoint backend.
 * Path ditulis relatif tanpa leading slash karena baseUrl diakhiri "/".
 */
interface AnimeApiService {

    @GET("anime/home")
    suspend fun getHome(): ApiResponse<HomeDataDto>

    @GET("anime/schedule")
    suspend fun getSchedule(): ApiResponse<List<ScheduleDayDto>>

    @GET("anime/anime/{id}")
    suspend fun getAnimeDetail(@Path("id") animeId: String): ApiResponse<AnimeDetailDto>

    @GET("anime/episode/{id}")
    suspend fun getEpisodeDetail(@Path("id") episodeId: String): ApiResponse<EpisodeDetailDto>

    /**
     * PENTING: serverId itu dinamis & bisa expired (pernah 404).
     * Selalu ambil fresh dari EpisodeDetailDto, jangan hardcode.
     * Bentuk `data` belum terdokumentasi pasti -> parse defensif di repository.
     */
    @GET("anime/server/{id}")
    suspend fun resolveServer(@Path("id") serverId: String): ApiResponse<JsonElement>
}
