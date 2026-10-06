package com.animedong.app.core.network

import com.animedong.app.data.dto.DonghuaDetailDto
import com.animedong.app.data.dto.DonghuaEpisodeDto
import com.animedong.app.data.dto.DonghuaHomeDto
import com.animedong.app.data.dto.DonghuaScheduleDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object DonghuaApiDefaults {
    // API donghua milik sendiri (Oracle VPS). Bisa diganti via Remote Config.
    const val BASE_URL = "http://168.110.213.108/donghua"
}

/**
 * API donghua — backend KEDUA, kontraknya beda dari API anime:
 * response TIDAK dibungkus {ok, data}, payload langsung di root.
 * Dibuat terpisah dari AnimeApiService karena base URL & format beda.
 */
interface DonghuaApiService {

    /**
     * Home: ?page=N -> {page, results[{section, cards[]}], total}.
     * Section yang dipakai: terpopuler_hari_ini, rilisan_terbaru, movie, rekomendasi.
     * Path "." = root base URL ("/" absolut akan membuang path /donghua).
     */
    @GET(".")
    suspend fun getHome(@Query("page") page: Int = 1): DonghuaHomeDto

    /** Jadwal mingguan: {days[], results[{day, items[]}]}. */
    @GET("schedule")
    suspend fun getSchedule(): DonghuaScheduleDto

    /**
     * Detail serial donghua: /{seriesSlug} -> {result{name, thumbnail, status,
     * tipe, rating, studio, genre[], sinopsis{paragraphs[]}, episode[]}}.
     * seriesSlug diturunkan dari slug kartu via DonghuaSlugs.
     */
    @GET("{slug}")
    suspend fun getDetail(@Path("slug") slug: String): DonghuaDetailDto

    /**
     * Server streaming episode: /episode/{episodeSlug} ->
     * {result{name, players[{name, url}]}}. URL-nya embed page langsung
     * (bukan file video), jadi player-nya WebView bukan ExoPlayer.
     */
    @GET("episode/{slug}")
    suspend fun getEpisode(@Path("slug") slug: String): DonghuaEpisodeDto
}
