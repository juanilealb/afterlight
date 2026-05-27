package com.juani.afterlight.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ArticleEntity?

    @Insert
    suspend fun insert(article: ArticleEntity): Long

    @Query("UPDATE articles SET lastReadPosition = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)
}
