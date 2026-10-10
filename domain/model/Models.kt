package com.animedong.app.domain.model

/** Tiga sumber konten: anime (sankavollerei), donghua (API VPS), kurama (animeapi). */
enum class ContentType(val key: String) {
    ANIME("anime"),
    DONGHUA("donghua"),
    KURAMA("kurama");

    companion object {
        fun fromKey(key: String?): ContentType =
            entries.firstOrNull { it.key == key } ?: ANIME
    }
}

/** Model bersih yang dipakai UI (dipetakan dari DTO di repository). */
data class AnimeSummary(
    val id: String,
    val title: String,
    val poster: String,
    val subtitle: String? = null,
    val contentType: ContentType = ContentType.ANIME
)

data class ScheduleDay(
    val day: String,
    val animeList: List<AnimeSummary>
)

data class AnimeDetail(
    val title: String,
    val poster: String,
    val japanese: String?,
    val score: String?,
    val status: String?,
    val type: String?,
    val studios: String?,
    val synopsis: String?,
    val genres: List<String>,
    val episodes: List<EpisodeItem>,
    val contentType: ContentType = ContentType.ANIME
)

data class EpisodeItem(
    val episodeId: String,
    val title: String,
    val number: Int?,
    val date: String?,
    val contentType: ContentType = ContentType.ANIME
)

data class EpisodeDetail(
    val title: String,
    val animeId: String,
    val defaultStreamingUrl: String?,
    val prevEpisodeId: String?,
    val nextEpisodeId: String?,
    val qualities: List<StreamQuality>
)

data class StreamQuality(
    val title: String,
    val servers: List<StreamServer>
)

data class StreamServer(
    val title: String,
    val serverId: String
)

// ---------- Donghua ----------

/** Home donghua: section-section asli dari API (bukan karangan). */
data class DonghuaHome(
    val popularToday: List<AnimeSummary>,
    val latest: List<AnimeSummary>,
    val movies: List<AnimeSummary>,
    val recommendations: List<AnimeSummary>
) {
    val isEmpty: Boolean get() =
        popularToday.isEmpty() && latest.isEmpty() &&
            movies.isEmpty() && recommendations.isEmpty()
}

/**
 * Stream episode donghua: daftar server embed langsung.
 * URL-nya halaman embed (bukan file video) -> dibuka pakai WebView.
 */
data class DonghuaStream(
    val title: String,
    val servers: List<DonghuaServer>
)

data class DonghuaServer(
    val name: String,
    val url: String
)
