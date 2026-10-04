package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        HistoryEntity::class,
        DownloadEntity::class,
        UserSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DongHiveDatabase : RoomDatabase() {
    abstract fun donghuaDao(): DonghuaDao

    companion object {
        @Volatile
        private var INSTANCE: DongHiveDatabase? = null

        fun getDatabase(context: Context): DongHiveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DongHiveDatabase::class.java,
                    "donghive_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
