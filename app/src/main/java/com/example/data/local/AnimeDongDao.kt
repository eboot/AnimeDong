package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDongDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE anime_id = :animeId)")
    fun isBookmarked(animeId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE anime_id = :animeId")
    suspend fun deleteBookmark(animeId: String)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY updated_at DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE episode_id = :episodeId LIMIT 1")
    suspend fun getHistoryByEpisode(episodeId: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE anime_id = :animeId ORDER BY updated_at DESC LIMIT 1")
    fun getLatestHistoryForAnime(animeId: String): Flow<WatchHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE episode_id = :episodeId")
    suspend fun deleteHistory(episodeId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()

    // User Session
    @Query("SELECT * FROM user_sessions WHERE id = 'current_user' LIMIT 1")
    fun getUserSession(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSession(session: UserSessionEntity)

    @Query("DELETE FROM user_sessions WHERE id = 'current_user'")
    suspend fun clearUserSession()
}
