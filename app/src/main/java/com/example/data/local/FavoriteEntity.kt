package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val donghuaId: String,
    val title: String,
    val chineseTitle: String,
    val coverUrl: String,
    val rating: Float,
    val status: String,
    val latestEpisode: Int,
    val genres: String, // comma separated
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val id: String, // donghuaId_episodeNumber
    val donghuaId: String,
    val donghuaTitle: String,
    val episodeNumber: Int,
    val coverUrl: String,
    val progressMs: Long,
    val durationMs: Long,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String, // donghuaId_episodeNumber
    val donghuaId: String,
    val donghuaTitle: String,
    val episodeNumber: Int,
    val coverUrl: String,
    val quality: String,
    val fileSizeMb: Float,
    val downloadStatus: String, // "Selesai", "Mengunduh", "Jeda"
    val progressPercent: Int = 100,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val id: String = "current_user",
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val idToken: String? = null,
    val isVip: Boolean = true,
    val loggedInAt: Long = System.currentTimeMillis()
)
