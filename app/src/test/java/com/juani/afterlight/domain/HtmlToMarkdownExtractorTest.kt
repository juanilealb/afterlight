package com.juani.afterlight.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlToMarkdownExtractorTest {
    @Test fun scoresArticleAndRemovesChrome() {
        val html = """
            <html><head>
              <meta property="og:title" content="Clean story">
              <meta name="author" content="Ada">
              <meta name="description" content="A compact description">
            </head><body>
              <nav>Home Subscribe Sign in</nav>
              <div class="sidebar related">Related links forever</div>
              <main>
                <article>
                  <h1>Clean story</h1>
                  <p>This is the first useful paragraph with enough words to look like a real article body.</p>
                  <p>This is the second useful paragraph, with <a href="/source">a source link</a> preserved.</p>
                  <figure><img src="/hero.jpg" alt="Hero"><figcaption>Main image</figcaption></figure>
                </article>
              </main>
            </body></html>
        """.trimIndent()

        val extracted = HtmlToMarkdownExtractor.extract(IncomingArticle(originalUrl = "https://example.com/post", html = html))

        assertEquals("Clean story", extracted.title)
        assertEquals("Ada", extracted.byline)
        assertTrue(extracted.extractionStrategy.startsWith("defuddle-inspired"))
        assertTrue(extracted.markdown.contains("# Clean story"))
        assertTrue(extracted.markdown.contains("[a source link](https://example.com/source)"))
        assertTrue(extracted.markdown.contains("![Hero](https://example.com/hero.jpg)"))
        assertFalse(extracted.markdown.contains("Subscribe Sign in"))
        assertFalse(extracted.markdown.contains("Related links"))
    }

    @Test fun fallsBackToPlainText() {
        val extracted = HtmlToMarkdownExtractor.extract(IncomingArticle(text = "My title\n\nBody text"))
        assertEquals("My title", extracted.title)
        assertEquals("Body text", extracted.markdown.lines().last())
        assertEquals("fallback:text", extracted.extractionStrategy)
    }
}
