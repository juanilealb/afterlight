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

    fun frontMatter(
        article: IncomingArticle,
        title: String,
        capturedAt: Instant,
        extracted: ExtractedArticle? = null,
    ): String = buildString {
        appendLine("---")
        appendYaml("title", title)
        firstNonBlank(extracted?.site, article.source)?.let { appendYaml("source", it) }
        extracted?.byline?.takeIf { it.isNotBlank() }?.let { appendYaml("author", it) }
        extracted?.published?.takeIf { it.isNotBlank() }?.let { appendYaml("published", it) }
        extracted?.description?.takeIf { it.isNotBlank() }?.let { appendYaml("description", it) }
        article.originalUrl?.takeIf { it.isNotBlank() }?.let { appendYaml("originalUrl", it) }
        article.resolvedUrl?.takeIf { it.isNotBlank() }?.let { appendYaml("resolvedUrl", it) }
        extracted?.extractionStrategy?.takeIf { it.isNotBlank() }?.let { appendYaml("extraction", it) }
        appendYaml("status", "unread")
        appendYaml("capturedAt", capturedAt.toString())
        appendLine("tags:")
        appendLine("  - afterlight")
        appendLine("  - read-later")
        appendLine("---")
        appendLine()
    }

    fun document(article: IncomingArticle, extracted: ExtractedArticle, capturedAt: Instant): String =
        frontMatter(article, extracted.title, capturedAt, extracted) + extracted.markdown.trim() + "\n"

    fun document(article: IncomingArticle, title: String, markdownBody: String, capturedAt: Instant): String =
        frontMatter(article, title, capturedAt) + markdownBody.trim() + "\n"

    fun displayBody(markdownDocument: String): String {
        val lines = markdownDocument.lineSequence().toList()
        if (lines.firstOrNull()?.trim() != "---") return markdownDocument.trim()
        val end = lines.drop(1).indexOfFirst { it.trim() == "---" }
        if (end == -1) return markdownDocument.trim()
        return lines.drop(end + 2).joinToString("\n").trim()
    }

    fun wordCount(markdown: String): Int = displayBody(markdown).split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) }

    fun readingMinutes(words: Int): Int = maxOf(1, (words + 219) / 220)

    private fun StringBuilder.appendYaml(key: String, value: String) {
        append(key).append(": ").appendLine('"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"')
    }

    private fun firstNonBlank(vararg values: String?): String? = values.firstOrNull { !it.isNullOrBlank() }
}
