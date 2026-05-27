package com.juani.afterlight.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class MarkdownArchiveTest {
    @Test fun slugNormalizesTitleAndDate() {
        val slug = MarkdownArchive.slug("La Nación: Qué pasó?", Instant.parse("2026-05-27T02:00:00Z"))
        assertEquals("2026-05-27-la-nacion-que-paso.md", slug)
    }

    @Test fun frontMatterEscapesQuotes() {
        val md = MarkdownArchive.frontMatter(IncomingArticle(originalUrl = "https://example.com/a"), "Title \"quoted\"", Instant.parse("2026-05-27T02:00:00Z"))
        assertTrue(md.contains("title: \"Title \\\"quoted\\\"\""))
        assertTrue(md.contains("originalUrl: \"https://example.com/a\""))
    }
}
