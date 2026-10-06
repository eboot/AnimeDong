package com.animedong.app.data.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// ---------- HOME: GET /?page=N ----------

data class DonghuaHomeDto(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("results") val results: List<DonghuaSectionDto> = emptyList()
)

data class DonghuaSectionDto(
    @SerializedName("section") val section: String = "",
    @SerializedName("cards") val cards: List<DonghuaCardDto> = emptyList()
)

/**
 * Kartu home adalah kartu EPISODE: slug = "{serial}-episode-{n}-subtitle-indonesia".
 * [seriesSlug] menurunkan slug serial-nya untuk navigasi ke detail.
 */
data class DonghuaCardDto(
    @SerializedName("eps") val eps: Int? = null,
    @SerializedName("slug") val slug: String = "",
    @SerializedName("thumbnail") val thumbnail: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("type") val type: String = ""
) {
    val seriesSlug: String get() = DonghuaSlugs.seriesSlugFromCard(slug)
}

// ---------- SCHEDULE: GET /schedule ----------

data class DonghuaScheduleDto(
    @SerializedName("results") val results: List<DonghuaScheduleDayDto> = emptyList()
)

data class DonghuaScheduleDayDto(
    @SerializedName("day") val day: String = "",
    @SerializedName("items") val items: List<DonghuaScheduleItemDto> = emptyList()
)

/** Item jadwal: slug-nya SUDAH slug serial (bukan slug episode). */
data class DonghuaScheduleItemDto(
    @SerializedName("eps") val eps: Int? = null,
    @SerializedName("slug") val slug: String = "",
    @SerializedName("thumbnail") val thumbnail: String = "",
    @SerializedName("title") val title: String = ""
)

// ---------- DETAIL: GET /{seriesSlug} ----------

data class DonghuaDetailDto(
    @SerializedName("result") val result: DonghuaDetailResultDto? = null
)

data class DonghuaDetailResultDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("thumbnail") val thumbnail: String = "",
    @SerializedName("status") val status: String? = null,
    @SerializedName("tipe") val tipe: String? = null,
    @SerializedName("rating") val rating: JsonElement? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("durasi") val durasi: String? = null,
    @SerializedName("tanggal_rilis") val tanggalRilis: String? = null,
    // genre: list string di API mentah, tapi parse defensif (bisa juga objek)
    @SerializedName("genre") val genre: List<JsonElement> = emptyList(),
    // sinopsis: {paragraphs[], title} — kadang string polos, parse di repository
    @SerializedName("sinopsis") val sinopsis: JsonElement? = null,
    @SerializedName("episode") val episode: List<DonghuaEpisodeItemDto> = emptyList()
)

data class DonghuaEpisodeItemDto(
    @SerializedName("date") val date: String? = null,
    // nomor episode berupa STRING di API ("130")
    @SerializedName("episode") val episode: String? = null,
    @SerializedName("slug") val slug: String = "",
    @SerializedName("subtitle") val subtitle: String? = null
)

// ---------- STREAM: GET /episode/{episodeSlug} ----------

data class DonghuaEpisodeDto(
    @SerializedName("result") val result: DonghuaStreamResultDto? = null
)

data class DonghuaStreamResultDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("players") val players: List<DonghuaPlayerDto> = emptyList()
)

data class DonghuaPlayerDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("url") val url: String = ""
)

/**
 * Derivasi slug serial dari slug kartu episode.
 * Contoh: "apotheosis-episode-130-subtitle-indonesia" -> "apotheosis".
 * Varian "-tamat-" juga ditangani; fallback potong suffix subtitle.
 * Mirror logika backend animedong-admin (scrape.js).
 */
object DonghuaSlugs {
    private val EP_SLUG_RE = Regex("^(.*)-episode-\\d+(?:-tamat)?-subtitle-indonesia$")
    private val SUFFIX_RE = Regex("-subtitle-indonesia?$")

    fun seriesSlugFromCard(cardSlug: String): String {
        val s = cardSlug.trim()
        if (s.isEmpty()) return ""
        EP_SLUG_RE.matchEntire(s)?.let { return it.groupValues[1] }
        return SUFFIX_RE.replace(s, "")
    }
}
