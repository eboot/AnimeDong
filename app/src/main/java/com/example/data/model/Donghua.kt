package com.example.data.model

data class Donghua(
    val id: String,
    val title: String,
    val chineseTitle: String,
    val synopsis: String,
    val coverUrl: String,
    val bannerUrl: String,
    val rating: Float,
    val status: String, // "Ongoing" or "Tamat"
    val releaseYear: String,
    val studio: String,
    val genres: List<String>,
    val totalEpisodes: Int,
    val latestEpisode: Int,
    val releaseDay: String, // "Senin", "Selasa", etc.
    val releaseTime: String, // "10:00 WIB"
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val views: String = "1.2M"
)

data class StreamServer(
    val id: String,
    val name: String,
    val quality: String,
    val videoUrl: String,
    val isVip: Boolean = false
)

data class Episode(
    val id: String,
    val donghuaId: String,
    val episodeNumber: Int,
    val title: String,
    val releaseDate: String,
    val durationMinutes: Int,
    val streamServers: List<StreamServer>
)

data class UserComment(
    val id: String,
    val donghuaId: String,
    val episodeNumber: Int,
    val userName: String,
    val avatarInitial: String,
    val commentText: String,
    val timeAgo: String,
    val likes: Int
)

data class ScheduleDay(
    val dayName: String,
    val dayLabel: String,
    val isToday: Boolean = false
)
