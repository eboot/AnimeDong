package com.example.data.remote

import com.example.data.remote.dto.AnimeDetailDto
import com.example.data.remote.dto.ApiResponse
import com.example.data.remote.dto.EpisodeStreamDto
import com.example.data.remote.dto.HomeDataDto
import com.example.data.remote.dto.ScheduleDayDto
import com.example.data.remote.dto.ServerUrlDto
import retrofit2.http.GET
import retrofit2.http.Path

interface AnimeApiService {

    @GET("/anime/home")
    suspend fun getHome(): ApiResponse<HomeDataDto>

    @GET("/anime/schedule")
    suspend fun getSchedule(): ApiResponse<List<ScheduleDayDto>>

    @GET("/anime/anime/{animeId}")
    suspend fun getAnimeDetail(@Path("animeId") animeId: String): ApiResponse<AnimeDetailDto>

    @GET("/anime/episode/{episodeId}")
    suspend fun getEpisodeStream(@Path("episodeId") episodeId: String): ApiResponse<EpisodeStreamDto>

    @GET("/anime/server/{serverId}")
    suspend fun getServerUrl(@Path("serverId") serverId: String): ApiResponse<ServerUrlDto>
}
