package com.juani.afterlight.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val source: String?,
    val originalUrl: String?,
    val resolvedUrl: String?,
    val markdownPath: String,
    val wordCount: Int,
    val estimatedReadingMinutes: Int,
    val status: String,
    val capturedAt: Long,
    val lastReadPosition: Int = 0,
)
