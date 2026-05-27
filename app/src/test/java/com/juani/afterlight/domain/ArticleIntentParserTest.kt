package com.juani.afterlight.domain

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArticleIntentParserTest {
    @Test fun parsesExplicitAfterlightIntent() {
        val intent = Intent(ArticleIntentParser.ACTION_SAVE_ARTICLE)
            .putExtra(ArticleIntentParser.EXTRA_TITLE, "  My Article  ")
            .putExtra(ArticleIntentParser.EXTRA_ORIGINAL_URL, "https://example.com/original")
            .putExtra(ArticleIntentParser.EXTRA_RESOLVED_URL, "https://removepaywalls.com/article")
            .putExtra(ArticleIntentParser.EXTRA_HTML, "<article>Hello</article>")
        val parsed = ArticleIntentParser.parse(intent)!!
        assertEquals("My Article", parsed.title)
        assertEquals("https://example.com/original", parsed.originalUrl)
        assertEquals("https://removepaywalls.com/article", parsed.resolvedUrl)
    }

    @Test fun parsesPlainTextShareUrl() {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "Read later https://example.com/story")
        val parsed = ArticleIntentParser.parse(intent)!!
        assertEquals("https://example.com/story", parsed.originalUrl)
    }
}
