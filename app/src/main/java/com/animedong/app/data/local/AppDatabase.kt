package com.animedong.app.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

/** Posisi terakhir nonton -> "Continue Watching". */
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val episodeId: String,
    val animeId: String,
    val title: String,
    val positionMs: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis(),
    // "anime" | "donghua" — biar navigasi dari Riwayat tahu layar tujuan.
    val contentType: String = "anime"
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val animeId: String,
    val title: String,
    val poster: String,
    val createdAt: Long = System.currentTimeMillis(),
    val contentType: String = "anime"
)

@Dao
interface WatchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<WatchHistoryEntity>
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE animeId = :animeId AND contentType = :contentType")
    suspend fun remove(animeId: String, contentType: String)

    @Query("SELECT * FROM bookmarks WHERE animeId = :animeId AND contentType = :contentType LIMIT 1")
    suspend fun get(animeId: String, contentType: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun getAll(): List<BookmarkEntity>
}

/** v1 -> v2: tambah kolom contentType (default "anime" untuk data lama). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE watch_history ADD COLUMN contentType TEXT NOT NULL DEFAULT 'anime'"
        )
        db.execSQL(
            "ALTER TABLE bookmarks ADD COLUMN contentType TEXT NOT NULL DEFAULT 'anime'"
        )
    }
}

@Database(
    entities = [WatchHistoryEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
}
