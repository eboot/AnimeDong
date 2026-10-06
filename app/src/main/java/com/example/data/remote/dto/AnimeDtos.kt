package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "ok") val ok: Boolean = true,
    @Json(name = "statusCode") val statusCode: Int = 200,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null
)

@JsonClass(generateAdapter = true)
data class AnimeItemDto(
    @Json(name = "animeId") val animeId: String? = null,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "poster") val poster: String? = null,
    @Json(name = "episodes") val episodes: Any? = null, // Can be Int or String in scraped APIs
    @Json(name = "releaseDay") val releaseDay: String? = null,
    @Json(name = "latestReleaseDate") val latestReleaseDate: String? = null,
    @Json(name = "rating") val rating: Any? = null
) {
    fun normalizedId(): String {
        return animeId ?: slug ?: title.lowercase().replace(" ", "-")
    }

    fun formattedEpisodes(): String {
        return when (episodes) {
            is Number -> "$episodes Eps"
            is String -> if (episodes.endsWith("Eps", ignoreCase = true)) episodes else "$episodes Eps"
            else -> "?"
        }
    }
}

@JsonClass(generateAdapter = true)
data class HomeDataDto(
    @Json(name = "ongoing") val ongoing: HomeCategoryDto? = null,
    @Json(name = "completed") val completed: HomeCategoryDto? = null
)

@JsonClass(generateAdapter = true)
data class HomeCategoryDto(
    @Json(name = "animeList") val animeList: List<AnimeItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class ScheduleDayDto(
    @Json(name = "day") val day: String = "",
    @Json(name = "anime_list") val animeList: List<AnimeItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class AnimeDetailDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "poster") val poster: String? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "rating") val rating: Any? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "releaseYear") val releaseYear: String? = null,
    @Json(name = "genres") val genres: List<String>? = null,
    @Json(name = "episodeList") val episodeList: List<EpisodeItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class EpisodeItemDto(
    @Json(name = "episodeId") val episodeId: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "eps") val eps: Any? = null,
    @Json(name = "date") val date: String? = null
)

@JsonClass(generateAdapter = true)
data class EpisodeStreamDto(
    @Json(name = "defaultStreamingUrl") val defaultStreamingUrl: String? = null,
    @Json(name = "server") val server: ServerContainerDto? = null,
    @Json(name = "prev") val prev: String? = null,
    @Json(name = "next") val next: String? = null
)

@JsonClass(generateAdapter = true)
data class ServerContainerDto(
    @Json(name = "qualities") val qualities: List<QualityContainerDto>? = null
)

@JsonClass(generateAdapter = true)
data class QualityContainerDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "serverList") val serverList: List<ServerItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class ServerItemDto(
    @Json(name = "title") val title: String = "",
    @Json(name = "serverId") val serverId: String = ""
)

@JsonClass(generateAdapter = true)
data class ServerUrlDto(
    @Json(name = "url") val url: String = ""
)
