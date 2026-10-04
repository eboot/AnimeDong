package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DonghuaDao {
    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE donghuaId = :donghuaId)")
    fun isFavorite(donghuaId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE donghuaId = :donghuaId")
    suspend fun deleteFavoriteById(donghuaId: String)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE donghuaId = :donghuaId ORDER BY lastWatchedAt DESC LIMIT 1")
    fun getLatestHistory(donghuaId: String): Flow<HistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)

    // User Session
    @Query("SELECT * FROM user_sessions WHERE id = 'current_user' LIMIT 1")
    fun getUserSession(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSession(session: UserSessionEntity)

    @Query("DELETE FROM user_sessions WHERE id = 'current_user'")
    suspend fun clearUserSession()
}
