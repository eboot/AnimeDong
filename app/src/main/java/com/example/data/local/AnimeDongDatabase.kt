package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchHistoryEntity::class,
        BookmarkEntity::class,
        UserSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnimeDongDatabase : RoomDatabase() {
    abstract fun animeDongDao(): AnimeDongDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeDongDatabase? = null

        fun getDatabase(context: Context): AnimeDongDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDongDatabase::class.java,
                    "animedong_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
