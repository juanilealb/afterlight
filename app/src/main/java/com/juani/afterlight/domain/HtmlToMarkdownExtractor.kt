package com.juani.afterlight.domain

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object HtmlToMarkdownExtractor {
    fun extract(input: IncomingArticle): ExtractedArticle {
        input.html?.takeIf { it.isNotBlank() }?.let { return fromHtml(it, input) }
        input.text?.takeIf { it.isNotBlank() }?.let {
            val title = input.title ?: it.lineSequence().firstOrNull { line -> line.isNotBlank() }?.take(90) ?: "Untitled"
            return ExtractedArticle(title, it.trim())
        }
        val title = input.title ?: input.originalUrl ?: input.resolvedUrl ?: "Untitled"
        return ExtractedArticle(title, "[Pendiente de extracción](${input.resolvedUrl ?: input.originalUrl ?: ""})")
    }

    private fun fromHtml(html: String, input: IncomingArticle): ExtractedArticle {
        val doc = Jsoup.parse(html)
        doc.select("script,style,noscript,iframe,svg,nav,footer,aside,form,button").remove()
        val title = input.title ?: doc.selectFirst("meta[property=og:title]")?.attr("content")?.takeIf { it.isNotBlank() }
            ?: doc.title().takeIf { it.isNotBlank() }
            ?: "Untitled"
        val article = doc.selectFirst("article") ?: largestTextBlock(doc.body()) ?: doc.body()
        val markdown = article.children().joinToString("\n\n") { it.toMarkdown() }
            .ifBlank { article.text() }
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
        return ExtractedArticle(title.trim(), markdown)
    }

    private fun largestTextBlock(root: Element?): Element? = root?.select("main, article, section, div")
        ?.maxByOrNull { it.text().length }

    private fun Element.toMarkdown(): String = when (tagName().lowercase()) {
        "h1" -> "# ${text()}"
        "h2" -> "## ${text()}"
        "h3" -> "### ${text()}"
        "h4" -> "#### ${text()}"
        "p" -> text()
        "blockquote" -> text().lineSequence().joinToString("\n") { "> $it" }
        "ul" -> children().joinToString("\n") { "- ${it.text()}" }
        "ol" -> children().mapIndexed { index, el -> "${index + 1}. ${el.text()}" }.joinToString("\n")
        "pre" -> "```\n${text()}\n```"
        else -> if (children().isNotEmpty()) children().joinToString("\n\n") { it.toMarkdown() } else text()
    }.trim()
}

data class ExtractedArticle(val title: String, val markdown: String)
