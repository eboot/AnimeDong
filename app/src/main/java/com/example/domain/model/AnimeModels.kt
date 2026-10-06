package com.example.domain.model

data class Anime(
    val id: String,
    val title: String,
    val poster: String,
    val episodes: String,
    val releaseDay: String,
    val latestReleaseDate: String
)

data class AnimeDetail(
    val id: String,
    val title: String,
    val poster: String,
    val synopsis: String,
    val rating: Float,
    val status: String,
    val releaseYear: String,
    val genres: List<String>,
    val episodes: List<EpisodeInfo>
)

data class EpisodeInfo(
    val episodeId: String,
    val title: String,
    val episodeNumber: String,
    val date: String
)

data class EpisodeStream(
    val defaultUrl: String?,
    val qualities: List<StreamQuality>,
    val prevEpisodeId: String?,
    val nextEpisodeId: String?
)

data class StreamQuality(
    val qualityTitle: String,
    val servers: List<ServerOption>
)

data class ServerOption(
    val title: String,
    val serverId: String
)

data class ScheduleDay(
    val day: String,
    val animeList: List<Anime>
)

data class WatchHistory(
    val episodeId: String,
    val animeId: String,
    val title: String,
    val positionMs: Long,
    val updatedAt: Long
)

data class Bookmark(
    val animeId: String,
    val title: String,
    val poster: String,
    val createdAt: Long
)
