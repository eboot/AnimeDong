package com.animedong.app.data.repository

import com.animedong.app.core.network.AnimeApiService
import com.animedong.app.data.dto.*
import com.animedong.app.data.local.AppDatabase
import com.animedong.app.data.local.BookmarkEntity
import com.animedong.app.data.local.WatchHistoryEntity
import com.animedong.app.domain.model.*
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Satu pintu data: mapping DTO -> domain model, plus akses Room.
 * Semua call network jalan di Dispatchers.IO.
 */
class AnimeRepository(
    private var api: AnimeApiService,
    private val db: AppDatabase
) {

    /** Ganti Retrofit service (dipakai saat Remote Config mengubah base URL). */
    fun updateApi(newApi: AnimeApiService) {
        api = newApi
    }

    // ---------- network ----------

    suspend fun getHome(): Pair<List<AnimeSummary>, List<AnimeSummary>> =
        withContext(Dispatchers.IO) {
            val data = api.getHome().data ?: throw Exception("Home kosong")
            data.ongoing?.animeList.orEmpty().map { it.toModel() } to
                data.completed?.animeList.orEmpty().map { it.toModel() }
        }

    suspend fun getSchedule(): List<ScheduleDay> = withContext(Dispatchers.IO) {
        val list = api.getSchedule().data ?: throw Exception("Jadwal kosong")
        list.map { day ->
            ScheduleDay(day.day, day.animeList.map { it.toModel() })
        }
    }

    suspend fun getAnimeDetail(animeId: String): AnimeDetail =
        withContext(Dispatchers.IO) {
            val d = api.getAnimeDetail(animeId).data
                ?: throw Exception("Anime tidak ditemukan")
            AnimeDetail(
                title = d.title,
                poster = d.poster,
                japanese = d.japanese,
                score = d.score,
                status = d.status,
                type = d.type,
                studios = d.studios,
                synopsis = d.synopsis?.paragraphs?.joinToString("\n\n"),
                genres = d.genreList.map { it.title },
                episodes = d.episodeList.map {
                    EpisodeItem(it.episodeId, it.title, it.eps, it.date)
                }
            )
        }

    suspend fun getEpisodeDetail(episodeId: String): EpisodeDetail =
        withContext(Dispatchers.IO) {
            val d = api.getEpisodeDetail(episodeId).data
                ?: throw Exception("Episode tidak ditemukan")
            EpisodeDetail(
                title = d.title,
                animeId = d.animeId,
                defaultStreamingUrl = d.defaultStreamingUrl,
                prevEpisodeId = d.prevEpisode.takeIf { d.hasPrevEpisode }?.episodeId,
                nextEpisodeId = d.nextEpisode.takeIf { d.hasNextEpisode }?.episodeId,
                qualities = d.server?.qualities.orEmpty().map { q ->
                    StreamQuality(
                        q.title,
                        q.serverList.map { StreamServer(it.title, it.serverId) }
                    )
                }
            )
        }

    /**
     * Resolve serverId -> URL video final.
     * Bentuk `data` belum pasti -> parse defensif, sesuaikan setelah
     * melihat response asli (kemarin cuma dapat 404 karena ID expired).
     */
    suspend fun resolveServerUrl(serverId: String): String? =
        withContext(Dispatchers.IO) {
            val data: JsonElement? = api.resolveServer(serverId).data
            when {
                data == null || data.isJsonNull -> null
                data.isJsonPrimitive -> data.asString
                data.isJsonObject -> {
                    val o = data.asJsonObject
                    listOf("url", "streamUrl", "videoUrl", "src")
                        .firstNotNullOfOrNull { key ->
                            o.get(key)?.takeIf { it.isJsonPrimitive }?.asString
                        }
                }
                else -> null
            }
        }

    // ---------- local (Room) ----------
    // Dipakai juga untuk konten donghua (contentType = DONGHUA) supaya
    // Koleksi/Riwayat bisa menampung dua tipe konten sekaligus.

    suspend fun saveProgress(
        animeId: String,
        episodeId: String,
        title: String,
        positionMs: Long,
        contentType: ContentType = ContentType.ANIME
    ) = withContext(Dispatchers.IO) {
        db.watchHistoryDao().upsert(
            WatchHistoryEntity(
                episodeId, animeId, title, positionMs,
                contentType = contentType.key
            )
        )
    }

    suspend fun getHistory() = withContext(Dispatchers.IO) {
        db.watchHistoryDao().getRecent()
    }

    suspend fun toggleBookmark(
        animeId: String,
        title: String,
        poster: String,
        contentType: ContentType = ContentType.ANIME
    ) = withContext(Dispatchers.IO) {
        val dao = db.bookmarkDao()
        if (dao.get(animeId, contentType.key) == null) {
            dao.add(BookmarkEntity(animeId, title, poster, contentType = contentType.key))
        } else {
            dao.remove(animeId, contentType.key)
        }
    }

    suspend fun isBookmarked(
        animeId: String,
        contentType: ContentType = ContentType.ANIME
    ) = withContext(Dispatchers.IO) {
        db.bookmarkDao().get(animeId, contentType.key) != null
    }

    suspend fun getBookmarks() = withContext(Dispatchers.IO) {
        db.bookmarkDao().getAll()
    }

    // ---------- mapping ----------

    private fun AnimeSummaryDto.toModel() = AnimeSummary(
        id = id,
        title = title,
        poster = poster,
        subtitle = latestReleaseDate
            ?: episodes?.let { "$it eps" }
            ?: releaseDay
    )
}
