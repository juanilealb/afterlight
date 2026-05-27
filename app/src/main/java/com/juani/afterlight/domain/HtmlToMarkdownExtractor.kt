package com.juani.afterlight.domain

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Defuddle/Obsidian-Clipper-inspired extraction pipeline for captured article HTML.
 *
 * The official Obsidian Web Clipper uses the JavaScript `defuddle` package. Afterlight runs
 * fully on-device in Android/Kotlin, so this is a native implementation of the same strategy:
 * clean noisy DOM, score article candidates, preserve semantic structure, and fall back honestly.
 */
object HtmlToMarkdownExtractor {
    fun extract(input: IncomingArticle): ExtractedArticle {
        input.html?.takeIf { it.isNotBlank() }?.let { return fromHtml(it, input) }
        input.text?.takeIf { it.isNotBlank() }?.let { return fromText(it, input) }
        val title = input.title ?: input.originalUrl ?: input.resolvedUrl ?: "Untitled"
        return ExtractedArticle(
            title = title,
            markdown = "[Pendiente de extracción](${input.resolvedUrl ?: input.originalUrl ?: ""})",
            byline = null,
            description = null,
            site = input.source,
            published = null,
            extractionStrategy = "fallback:url-only",
        )
    }

    private fun fromText(text: String, input: IncomingArticle): ExtractedArticle {
        val trimmed = text.trim()
        val title = input.title ?: trimmed.lineSequence().firstOrNull { it.isNotBlank() }?.take(90) ?: "Untitled"
        return ExtractedArticle(
            title = title,
            markdown = trimmed,
            byline = null,
            description = null,
            site = input.source,
            published = null,
            extractionStrategy = "fallback:text",
        )
    }

    private fun fromHtml(html: String, input: IncomingArticle): ExtractedArticle {
        val baseUrl = input.originalUrl ?: input.resolvedUrl ?: ""
        val doc = Jsoup.parse(html, baseUrl)
        doc.outputSettings().prettyPrint(false)
        doc.select(NOISE_SELECTOR).remove()

        val metadata = doc.metadata(input)
        val article = findArticleRoot(doc)
        article.select(INLINE_NOISE_SELECTOR).remove()

        val markdown = article.childNodes().joinToString("\n\n") { it.toMarkdown(baseUrl) }
            .ifBlank { article.text() }
            .normalizeMarkdown()

        val body = markdown.ifBlank { doc.body()?.text()?.trim().orEmpty() }
        return ExtractedArticle(
            title = metadata.title,
            markdown = body,
            byline = metadata.byline,
            description = metadata.description,
            site = metadata.site,
            published = metadata.published,
            extractionStrategy = metadata.strategy,
        )
    }

    private fun Document.metadata(input: IncomingArticle): ExtractedMetadata {
        val title = firstNonBlank(
            input.title,
            meta("property", "og:title"),
            meta("name", "twitter:title"),
            selectFirst("h1")?.text(),
            title(),
        ) ?: "Untitled"
        val byline = firstNonBlank(
            meta("name", "author"),
            meta("property", "article:author"),
            selectFirst("[rel=author], .byline, [class*=byline], [class*=author]")?.text(),
        )
        val site = firstNonBlank(
            input.source,
            meta("property", "og:site_name"),
            location().takeIf { it.isNotBlank() }?.let { runCatching { java.net.URI(it).host }.getOrNull()?.removePrefix("www.") },
        )
        val description = firstNonBlank(
            meta("name", "description"),
            meta("property", "og:description"),
            meta("name", "twitter:description"),
        )
        val published = firstNonBlank(
            meta("property", "article:published_time"),
            meta("name", "date"),
            selectFirst("time[datetime]")?.attr("datetime"),
        )
        return ExtractedMetadata(title.trim(), byline?.trim(), description?.trim(), site?.trim(), published?.trim(), "defuddle-inspired:dom-score")
    }

    private fun Document.meta(attr: String, key: String): String? =
        selectFirst("meta[$attr=$key]")?.attr("content")?.takeIf { it.isNotBlank() }

    private fun findArticleRoot(doc: Document): Element {
        doc.selectFirst("article")?.takeIf { it.text().wordishLength() > 300 }?.let { return it }
        doc.selectFirst("main")?.takeIf { it.text().wordishLength() > 450 }?.let { return it }
        return doc.body()
            ?.select("article, main, section, div, [role=main]")
            ?.maxByOrNull { it.articleScore() }
            ?.takeIf { it.text().wordishLength() > 120 }
            ?: doc.body()
            ?: doc
    }

    private fun Element.articleScore(): Double {
        val text = text().trim()
        if (text.length < 80) return 0.0
        val paragraphScore = select("p").sumOf { p ->
            val len = p.text().wordishLength()
            when {
                len > 900 -> 5.0
                len > 450 -> 3.0
                len > 140 -> 1.5
                else -> 0.2
            }
        }
        val linkText = select("a").sumOf { it.text().length }.coerceAtLeast(1)
        val linkDensity = linkText.toDouble() / text.length.coerceAtLeast(1)
        val classAndId = listOf(className(), id()).joinToString(" ").lowercase()
        val positive = POSITIVE_HINTS.count { it in classAndId } * 35.0
        val negative = NEGATIVE_HINTS.count { it in classAndId } * 45.0
        val headingBonus = if (selectFirst("h1, h2") != null) 20.0 else 0.0
        return text.wordishLength() + paragraphScore + positive + headingBonus - negative - (linkDensity * 350.0)
    }

    private fun Node.toMarkdown(baseUrl: String): String = when (this) {
        is TextNode -> text().trim()
        is Element -> toMarkdown(baseUrl)
        else -> childNodes().joinToString("") { it.toMarkdown(baseUrl) }.trim()
    }

    private fun Element.toMarkdown(baseUrl: String): String {
        val inner = childNodes().joinToString("") { it.toMarkdown(baseUrl) }.trim()
        return when (normalName()) {
            "h1" -> "# ${text().trim()}"
            "h2" -> "## ${text().trim()}"
            "h3" -> "### ${text().trim()}"
            "h4" -> "#### ${text().trim()}"
            "h5" -> "##### ${text().trim()}"
            "h6" -> "###### ${text().trim()}"
            "p" -> inner.ifBlank { text().trim() }
            "br" -> "\n"
            "blockquote" -> text().lineSequence().filter { it.isNotBlank() }.joinToString("\n") { "> ${it.trim()}" }
            "ul" -> children().filter { it.normalName() == "li" }.joinToString("\n") { "- ${it.text().trim()}" }
            "ol" -> children().filter { it.normalName() == "li" }.mapIndexed { i, li -> "${i + 1}. ${li.text().trim()}" }.joinToString("\n")
            "li" -> "- ${text().trim()}"
            "pre" -> "```\n${text().trim()}\n```"
            "code" -> if (parent()?.normalName() == "pre") text() else "`${text().trim()}`"
            "strong", "b" -> "**${inner.ifBlank { text().trim() }}**"
            "em", "i" -> "*${inner.ifBlank { text().trim() }}*"
            "a" -> linkMarkdown(baseUrl)
            "img" -> imageMarkdown(baseUrl)
            "figure" -> figureMarkdown(baseUrl)
            "table" -> tableMarkdown()
            "hr" -> "---"
            "script", "style", "noscript" -> ""
            else -> if (children().isNotEmpty()) children().joinToString("\n\n") { it.toMarkdown(baseUrl) } else text().trim()
        }.trim()
    }

    private fun Element.linkMarkdown(baseUrl: String): String {
        val label = text().trim().ifBlank { attr("href") }
        val href = absUrl("href").ifBlank { attr("href") }
        if (href.isBlank()) return label
        if (label == href) return href
        return "[$label]($href)"
    }

    private fun Element.imageMarkdown(baseUrl: String): String {
        val src = absUrl("src").ifBlank { attr("src") }
        if (src.isBlank()) return ""
        val alt = attr("alt").ifBlank { attr("title") }.trim()
        return "![$alt]($src)"
    }

    private fun Element.figureMarkdown(baseUrl: String): String {
        val image = selectFirst("img")?.imageMarkdown(baseUrl).orEmpty()
        val caption = selectFirst("figcaption")?.text()?.trim().orEmpty()
        return listOf(image, caption.takeIf { it.isNotBlank() }?.let { "_${it}_" }).filterNotNull().joinToString("\n\n")
    }

    private fun Element.tableMarkdown(): String {
        val rows = select("tr").map { row -> row.select("th,td").map { it.text().replace("|", "\\|").trim() } }.filter { it.isNotEmpty() }
        if (rows.isEmpty()) return text().trim()
        val header = rows.first()
        val separator = header.map { "---" }
        val body = rows.drop(1)
        return (listOf(header, separator) + body).joinToString("\n") { cells -> "| " + cells.joinToString(" | ") + " |" }
    }

    private fun String.normalizeMarkdown(): String =
        replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .lineSequence()
            .map { it.trimEnd() }
            .joinToString("\n")
            .trim()

    private fun String.wordishLength(): Int = split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) }

    private fun firstNonBlank(vararg values: String?): String? = values.firstOrNull { !it.isNullOrBlank() }

    private const val NOISE_SELECTOR = "script, style, noscript, iframe, svg, canvas, nav, footer, aside, form, button, input, textarea, select, header, .ad, .ads, .advertisement, .newsletter, .subscribe, .paywall, .social, .share, .related, .recommendations, [class*=cookie], [id*=cookie], [class*=promo], [class*=modal]"
    private const val INLINE_NOISE_SELECTOR = ".ad, .ads, .advertisement, .newsletter, .subscribe, .social, .share, .related, .recommendations, [class*=cookie], [id*=cookie], [class*=promo], [class*=modal]"
    private val POSITIVE_HINTS = listOf("article", "content", "entry", "post", "story", "body", "main")
    private val NEGATIVE_HINTS = listOf("comment", "footer", "masthead", "promo", "related", "share", "sidebar", "nav", "menu")
}

data class ExtractedArticle(
    val title: String,
    val markdown: String,
    val byline: String? = null,
    val description: String? = null,
    val site: String? = null,
    val published: String? = null,
    val extractionStrategy: String = "unknown",
)

private data class ExtractedMetadata(
    val title: String,
    val byline: String?,
    val description: String?,
    val site: String?,
    val published: String?,
    val strategy: String,
)
