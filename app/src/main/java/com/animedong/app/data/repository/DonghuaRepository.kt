package com.animedong.app.data.repository

import com.animedong.app.core.network.DonghuaApiService
import com.animedong.app.data.dto.DonghuaCardDto
import com.animedong.app.data.dto.DonghuaScheduleItemDto
import com.animedong.app.domain.model.AnimeDetail
import com.animedong.app.domain.model.AnimeSummary
import com.animedong.app.domain.model.ContentType
import com.animedong.app.domain.model.DonghuaHome
import com.animedong.app.domain.model.DonghuaServer
import com.animedong.app.domain.model.DonghuaStream
import com.animedong.app.domain.model.EpisodeItem
import com.animedong.app.domain.model.ScheduleDay
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Satu pintu data donghua (API VPS sendiri).
 * Network-only: penyimpanan lokal (bookmark/riwayat) tetap lewat
 * AnimeRepository dengan contentType = DONGHUA.
 */
class DonghuaRepository(
    private var api: DonghuaApiService
) {

    /** Ganti service (dipakai saat Remote Config mengubah base URL). */
    fun updateApi(newApi: DonghuaApiService) {
        api = newApi
    }

    suspend fun getHome(page: Int = 1): DonghuaHome = withContext(Dispatchers.IO) {
        val dto = api.getHome(page)
        val bySection = dto.results.associate { it.section to it.cards }
        val home = DonghuaHome(
            popularToday = bySection["terpopuler_hari_ini"].orEmpty().map { it.toSummary() },
            latest = bySection["rilisan_terbaru"].orEmpty().map { it.toSummary() },
            movies = bySection["movie"].orEmpty().map { it.toSummary() },
            recommendations = bySection["rekomendasi"].orEmpty().map { it.toSummary() }
        )
        if (home.isEmpty) throw Exception("Donghua kosong")
        home
    }

    suspend fun getSchedule(): List<ScheduleDay> = withContext(Dispatchers.IO) {
        api.getSchedule().results.map { day ->
            ScheduleDay(
                day = normalizeDay(day.day),
                animeList = day.items.map { it.toSummary() }
            )
        }
    }

    suspend fun getDetail(seriesSlug: String): AnimeDetail =
        withContext(Dispatchers.IO) {
            val r = api.getDetail(seriesSlug).result
                ?: throw Exception("Donghua tidak ditemukan")
            AnimeDetail(
                title = r.name,
                poster = r.thumbnail,
                japanese = null,
                score = r.rating?.asPlainString(),
                status = r.status,
                type = r.tipe,
                studios = r.studio,
                synopsis = r.sinopsis?.asParagraphs(),
                genres = r.genre.mapNotNull { it.asGenreTitle() },
                episodes = r.episode.map {
                    EpisodeItem(
                        episodeId = it.slug,
                        title = it.subtitle?.takeIf { s -> s.isNotBlank() }
                            ?: "Episode ${it.episode ?: "?"}",
                        number = it.episode?.toIntOrNull(),
                        date = it.date,
                        contentType = ContentType.DONGHUA
                    )
                },
                contentType = ContentType.DONGHUA
            )
        }

    /**
     * Daftar server embed langsung sebuah episode.
     * URL-nya halaman embed (ok.ru, abyssplayer, dsb) -> player pakai WebView.
     */
    suspend fun getStream(episodeSlug: String): DonghuaStream =
        withContext(Dispatchers.IO) {
            val r = api.getEpisode(episodeSlug).result
                ?: throw Exception("Server tidak ditemukan")
            val servers = r.players
                .filter { it.url.isNotBlank() }
                .map { DonghuaServer(it.name.ifBlank { "Server" }, it.url) }
            if (servers.isEmpty()) throw Exception("Tidak ada server streaming")
            DonghuaStream(title = r.name.ifBlank { "Episode" }, servers = servers)
        }

    // ---------- mapping ----------

    private fun DonghuaCardDto.toSummary() = AnimeSummary(
        id = seriesSlug,
        title = title,
        poster = thumbnail,
        subtitle = eps?.let { "Episode $it" } ?: type.takeIf { it.isNotBlank() },
        contentType = ContentType.DONGHUA
    )

    private fun DonghuaScheduleItemDto.toSummary() = AnimeSummary(
        id = slug,
        title = title,
        poster = thumbnail,
        subtitle = eps?.let { "Episode $it" },
        contentType = ContentType.DONGHUA
    )

    /** "Jum'at" -> "Jumat" supaya cocok dengan daftar hari di UI. */
    private fun normalizeDay(day: String): String =
        if (day.trim().equals("Jum'at", ignoreCase = true)) "Jumat" else day.trim()

    /** rating bisa string/number/null di JSON -> ambil sebagai string polos. */
    private fun JsonElement.asPlainString(): String? {
        if (isJsonNull) return null
        if (!isJsonPrimitive) return null
        return asJsonPrimitive.asString.takeIf { it.isNotBlank() }
    }

    /** genre: string polos di API mentah; tahan juga kalau suatu saat jadi objek. */
    private fun JsonElement.asGenreTitle(): String? {
        if (isJsonNull) return null
        if (isJsonPrimitive) return asJsonPrimitive.asString.takeIf { it.isNotBlank() }
        if (isJsonObject) {
            return asJsonObject.get("title")
                ?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        }
        return null
    }

    /** sinopsis: {paragraphs[]} atau string polos. */
    private fun JsonElement.asParagraphs(): String? {
        if (isJsonNull) return null
        if (isJsonPrimitive) return asJsonPrimitive.asString.takeIf { it.isNotBlank() }
        if (isJsonObject) {
            val paragraphs = asJsonObject.get("paragraphs")
            if (paragraphs != null && paragraphs.isJsonArray) {
                val text = paragraphs.asJsonArray
                    .filter { it.isJsonPrimitive }
                    .joinToString("\n\n") { it.asString.trim() }
                    .trim()
                if (text.isNotEmpty()) return text
            }
        }
        return null
    }
}
