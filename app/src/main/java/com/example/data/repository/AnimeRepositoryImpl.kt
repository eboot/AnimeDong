package com.example.data.repository

import com.example.data.local.AnimeDongDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.remote.ApiClientProvider
import com.example.data.remote.dto.AnimeItemDto
import com.example.domain.model.Anime
import com.example.domain.model.AnimeDetail
import com.example.domain.model.Bookmark
import com.example.domain.model.EpisodeInfo
import com.example.domain.model.EpisodeStream
import com.example.domain.model.ScheduleDay
import com.example.domain.model.ServerOption
import com.example.domain.model.StreamQuality
import com.example.domain.model.WatchHistory
import com.example.domain.repository.AnimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class AnimeRepositoryImpl(
    private val dao: AnimeDongDao,
    private val apiClientProvider: ApiClientProvider = ApiClientProvider.getInstance()
) : AnimeRepository {

    private fun mapDtoToAnime(dto: AnimeItemDto): Anime {
        return Anime(
            id = dto.normalizedId(),
            title = dto.title,
            poster = dto.poster.orEmpty(),
            episodes = dto.formattedEpisodes(),
            releaseDay = dto.releaseDay.orEmpty(),
            latestReleaseDate = dto.latestReleaseDate.orEmpty()
        )
    }

    override fun getHomeOngoing(): Flow<Result<List<Anime>>> = flow {
        try {
            val response = apiClientProvider.getApiService().getHome()
            val list = response.data?.ongoing?.animeList?.map { mapDtoToAnime(it) } ?: emptyList()
            emit(Result.success(list))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHomeCompleted(): Flow<Result<List<Anime>>> = flow {
        try {
            val response = apiClientProvider.getApiService().getHome()
            val list = response.data?.completed?.animeList?.map { mapDtoToAnime(it) } ?: emptyList()
            emit(Result.success(list))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSchedule(): Flow<Result<List<ScheduleDay>>> = flow {
        try {
            val response = apiClientProvider.getApiService().getSchedule()
            val schedule = response.data?.map { dayDto ->
                ScheduleDay(
                    day = dayDto.day,
                    animeList = dayDto.animeList?.map { mapDtoToAnime(it) } ?: emptyList()
                )
            } ?: emptyList()
            emit(Result.success(schedule))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetail(animeId: String): Flow<Result<AnimeDetail>> = flow {
        try {
            val response = apiClientProvider.getApiService().getAnimeDetail(animeId)
            val data = response.data ?: throw IllegalStateException("Detail anime tidak ditemukan")
            val ratingFloat = when (val r = data.rating) {
                is Number -> r.toFloat()
                is String -> r.toFloatOrNull() ?: 0.0f
                else -> 0.0f
            }
            val detail = AnimeDetail(
                id = animeId,
                title = data.title,
                poster = data.poster.orEmpty(),
                synopsis = data.synopsis.orEmpty(),
                rating = ratingFloat,
                status = data.status ?: "Ongoing",
                releaseYear = data.releaseYear.orEmpty(),
                genres = data.genres ?: emptyList(),
                episodes = data.episodeList?.map { ep ->
                    EpisodeInfo(
                        episodeId = ep.episodeId,
                        title = ep.title,
                        episodeNumber = ep.eps?.toString() ?: "",
                        date = ep.date.orEmpty()
                    )
                } ?: emptyList()
            )
            emit(Result.success(detail))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getEpisodeStream(episodeId: String): Flow<Result<EpisodeStream>> = flow {
        try {
            val response = apiClientProvider.getApiService().getEpisodeStream(episodeId)
            val data = response.data ?: throw IllegalStateException("Stream episode tidak ditemukan")
            val qualities = data.server?.qualities?.map { q ->
                StreamQuality(
                    qualityTitle = q.title,
                    servers = q.serverList?.map { s ->
                        ServerOption(title = s.title, serverId = s.serverId)
                    } ?: emptyList()
                )
            } ?: emptyList()

            val stream = EpisodeStream(
                defaultUrl = data.defaultStreamingUrl,
                qualities = qualities,
                prevEpisodeId = data.prev,
                nextEpisodeId = data.next
            )
            emit(Result.success(stream))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getServerFinalUrl(serverId: String): Flow<Result<String>> = flow {
        try {
            val response = apiClientProvider.getApiService().getServerUrl(serverId)
            val url = response.data?.url ?: throw IllegalStateException("URL server tidak ditemukan")
            emit(Result.success(url))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return dao.getAllBookmarks().map { list ->
            list.map { Bookmark(it.animeId, it.title, it.poster, it.createdAt) }
        }
    }

    override fun isBookmarked(animeId: String): Flow<Boolean> {
        return dao.isBookmarked(animeId)
    }

    override suspend fun toggleBookmark(animeId: String, title: String, poster: String) {
        val currentlyBookmarked = dao.isBookmarked(animeId).first()
        if (currentlyBookmarked) {
            dao.deleteBookmark(animeId)
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    animeId = animeId,
                    title = title,
                    poster = poster
                )
            )
        }
    }

    override fun getHistory(): Flow<List<WatchHistory>> {
        return dao.getAllHistory().map { list ->
            list.map { WatchHistory(it.episodeId, it.animeId, it.title, it.positionMs, it.updatedAt) }
        }
    }

    override suspend fun saveHistory(
        episodeId: String,
        animeId: String,
        title: String,
        positionMs: Long
    ) {
        dao.insertHistory(
            WatchHistoryEntity(
                episodeId = episodeId,
                animeId = animeId,
                title = title,
                positionMs = positionMs,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteHistory(episodeId: String) {
        dao.deleteHistory(episodeId)
    }

    override suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }
}
