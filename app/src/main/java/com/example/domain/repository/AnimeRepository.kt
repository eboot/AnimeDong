package com.example.domain.repository

import com.example.domain.model.Anime
import com.example.domain.model.AnimeDetail
import com.example.domain.model.Bookmark
import com.example.domain.model.EpisodeStream
import com.example.domain.model.ScheduleDay
import com.example.domain.model.WatchHistory
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun getHomeOngoing(): Flow<Result<List<Anime>>>
    fun getHomeCompleted(): Flow<Result<List<Anime>>>
    fun getSchedule(): Flow<Result<List<ScheduleDay>>>
    fun getAnimeDetail(animeId: String): Flow<Result<AnimeDetail>>
    fun getEpisodeStream(episodeId: String): Flow<Result<EpisodeStream>>
    fun getServerFinalUrl(serverId: String): Flow<Result<String>>

    fun getBookmarks(): Flow<List<Bookmark>>
    fun isBookmarked(animeId: String): Flow<Boolean>
    suspend fun toggleBookmark(animeId: String, title: String, poster: String)

    fun getHistory(): Flow<List<WatchHistory>>
    suspend fun saveHistory(episodeId: String, animeId: String, title: String, positionMs: Long)
    suspend fun deleteHistory(episodeId: String)
    suspend fun clearAllHistory()
}
