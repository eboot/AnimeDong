package com.animedong.app.data.dto

import com.google.gson.annotations.SerializedName

// ---------- HOME: GET /anime/home ----------

data class HomeDataDto(
    @SerializedName("ongoing") val ongoing: AnimeSectionDto?,
    @SerializedName("completed") val completed: AnimeSectionDto?
)

data class AnimeSectionDto(
    @SerializedName("animeList") val animeList: List<AnimeSummaryDto> = emptyList()
)

/** Backend kadang pakai key `animeId`, kadang `slug` -> dua-duanya dibaca. */
data class AnimeSummaryDto(
    @SerializedName("animeId") val animeId: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("poster") val poster: String = "",
    @SerializedName("episodes") val episodes: Int? = null,
    @SerializedName("releaseDay") val releaseDay: String? = null,
    @SerializedName("latestReleaseDate") val latestReleaseDate: String? = null
) {
    val id: String get() = animeId ?: slug ?: ""
}

// ---------- SCHEDULE: GET /anime/schedule ----------

data class ScheduleDayDto(
    @SerializedName("day") val day: String = "",
    @SerializedName("anime_list") val animeList: List<AnimeSummaryDto> = emptyList()
)

// ---------- DETAIL: GET /anime/anime/{id} ----------

data class AnimeDetailDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("poster") val poster: String = "",
    @SerializedName("japanese") val japanese: String? = null,
    @SerializedName("score") val score: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("episodes") val episodes: Int? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("aired") val aired: String? = null,
    @SerializedName("studios") val studios: String? = null,
    @SerializedName("synopsis") val synopsis: SynopsisDto? = null,
    @SerializedName("genreList") val genreList: List<GenreDto> = emptyList(),
    @SerializedName("episodeList") val episodeList: List<EpisodeItemDto> = emptyList()
)

data class SynopsisDto(
    @SerializedName("paragraphs") val paragraphs: List<String> = emptyList()
)

data class GenreDto(
    @SerializedName("title") val title: String = ""
)

data class EpisodeItemDto(
    @SerializedName("episodeId") val episodeId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("eps") val eps: Int? = null,
    @SerializedName("date") val date: String? = null
)

// ---------- EPISODE: GET /anime/episode/{id} ----------

data class EpisodeDetailDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("animeId") val animeId: String = "",
    @SerializedName("defaultStreamingUrl") val defaultStreamingUrl: String? = null,
    @SerializedName("hasPrevEpisode") val hasPrevEpisode: Boolean = false,
    @SerializedName("prevEpisode") val prevEpisode: EpisodeRefDto? = null,
    @SerializedName("hasNextEpisode") val hasNextEpisode: Boolean = false,
    @SerializedName("nextEpisode") val nextEpisode: EpisodeRefDto? = null,
    @SerializedName("server") val server: ServerDto? = null
)

data class EpisodeRefDto(
    @SerializedName("episodeId") val episodeId: String = ""
)

data class ServerDto(
    @SerializedName("qualities") val qualities: List<StreamQualityDto> = emptyList()
)

data class StreamQualityDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("serverList") val serverList: List<StreamServerDto> = emptyList()
)

data class StreamServerDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("serverId") val serverId: String = ""
)
