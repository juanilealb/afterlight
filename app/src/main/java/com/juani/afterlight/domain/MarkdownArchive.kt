package com.juani.afterlight.domain

import java.text.Normalizer
import java.time.Instant

object MarkdownArchive {
    fun slug(title: String, capturedAt: Instant): String {
        val normalized = Normalizer.normalize(title.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "article" }
            .take(72)
            .trim('-')
        return "${capturedAt.toString().take(10)}-$normalized.md"
    }

    fun frontMatter(article: IncomingArticle, title: String, capturedAt: Instant): String = buildString {
        appendLine("---")
        appendYaml("title", title)
        article.source?.takeIf { it.isNotBlank() }?.let { appendYaml("source", it) }
        article.originalUrl?.takeIf { it.isNotBlank() }?.let { appendYaml("originalUrl", it) }
        article.resolvedUrl?.takeIf { it.isNotBlank() }?.let { appendYaml("resolvedUrl", it) }
        appendYaml("capturedAt", capturedAt.toString())
        appendLine("---")
        appendLine()
    }

    fun document(article: IncomingArticle, title: String, markdownBody: String, capturedAt: Instant): String =
        frontMatter(article, title, capturedAt) + markdownBody.trim() + "\n"

    fun wordCount(markdown: String): Int = markdown.split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) }

    fun readingMinutes(words: Int): Int = maxOf(1, (words + 219) / 220)

    private fun StringBuilder.appendYaml(key: String, value: String) {
        append(key).append(": ").appendLine('"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"')
    }
}
