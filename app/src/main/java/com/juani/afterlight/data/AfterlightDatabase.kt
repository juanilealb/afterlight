package com.juani.afterlight.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)
abstract class AfterlightDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    companion object {
        fun create(context: Context): AfterlightDatabase = Room.databaseBuilder(
            context.applicationContext,
            AfterlightDatabase::class.java,
            "afterlight.db",
        ).build()
    }
}
