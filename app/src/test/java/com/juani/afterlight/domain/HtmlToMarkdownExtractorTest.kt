package com.juani.afterlight.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlToMarkdownExtractorTest {
    @Test fun extractsArticleBodyToMarkdown() {
        val html = """
            <html><head><title>Ignored</title><meta property='og:title' content='Readable Title'></head>
            <body><nav>menu</nav><article><h1>Readable Title</h1><p>First paragraph.</p><p>Second paragraph.</p></article></body></html>
        """.trimIndent()
        val extracted = HtmlToMarkdownExtractor.extract(IncomingArticle(html = html))
        assertEquals("Readable Title", extracted.title)
        assertTrue(extracted.markdown.contains("# Readable Title"))
        assertTrue(extracted.markdown.contains("First paragraph."))
        assertTrue(!extracted.markdown.contains("menu"))
    }
}
