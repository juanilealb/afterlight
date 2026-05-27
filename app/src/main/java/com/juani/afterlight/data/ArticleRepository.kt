package com.juani.afterlight.data

import android.content.Context
import com.juani.afterlight.domain.HtmlToMarkdownExtractor
import com.juani.afterlight.domain.IncomingArticle
import com.juani.afterlight.domain.MarkdownArchive
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.Instant

class ArticleRepository(
    private val context: Context,
    private val dao: ArticleDao,
) {
    fun observeArticles(): Flow<List<ArticleEntity>> = dao.observeAll()

    suspend fun save(incoming: IncomingArticle): Long {
        val capturedAt = Instant.now()
        val extracted = HtmlToMarkdownExtractor.extract(incoming)
        val title = extracted.title.ifBlank { incoming.title ?: "Untitled" }
        val markdown = MarkdownArchive.document(incoming, title, extracted.markdown, capturedAt)
        val dir = File(context.filesDir, "markdown").apply { mkdirs() }
        val file = File(dir, MarkdownArchive.slug(title, capturedAt))
        file.writeText(markdown)
        val words = MarkdownArchive.wordCount(markdown)
        return dao.insert(
            ArticleEntity(
                title = title,
                source = incoming.source,
                originalUrl = incoming.originalUrl,
                resolvedUrl = incoming.resolvedUrl,
                markdownPath = file.absolutePath,
                wordCount = words,
                estimatedReadingMinutes = MarkdownArchive.readingMinutes(words),
                status = "READY",
                capturedAt = capturedAt.toEpochMilli(),
            ),
        )
    }

    suspend fun getMarkdown(id: Long): Pair<ArticleEntity, String>? = dao.get(id)?.let { it to File(it.markdownPath).readText() }
}
