package com.juani.afterlight.domain

import android.content.Intent
import android.net.Uri

object ArticleIntentParser {
    const val ACTION_SAVE_ARTICLE = "com.juani.afterlight.SAVE_ARTICLE"
    const val EXTRA_TITLE = "title"
    const val EXTRA_SOURCE = "source"
    const val EXTRA_ORIGINAL_URL = "originalUrl"
    const val EXTRA_RESOLVED_URL = "resolvedUrl"
    const val EXTRA_HTML = "html"
    const val EXTRA_TEXT = "text"

    fun parse(intent: Intent?): IncomingArticle? {
        if (intent == null) return null
        return when (intent.action) {
            ACTION_SAVE_ARTICLE -> parseExplicit(intent)
            Intent.ACTION_SEND -> parseShare(intent)
            else -> null
        }?.takeIf { !it.html.isNullOrBlank() || !it.text.isNullOrBlank() || !it.originalUrl.isNullOrBlank() || !it.resolvedUrl.isNullOrBlank() }
    }

    private fun parseExplicit(intent: Intent): IncomingArticle = IncomingArticle(
        title = intent.getStringExtra(EXTRA_TITLE)?.clean(),
        source = intent.getStringExtra(EXTRA_SOURCE)?.clean(),
        originalUrl = intent.getStringExtra(EXTRA_ORIGINAL_URL)?.clean(),
        resolvedUrl = intent.getStringExtra(EXTRA_RESOLVED_URL)?.clean(),
        html = intent.getStringExtra(EXTRA_HTML),
        text = intent.getStringExtra(EXTRA_TEXT) ?: intent.getStringExtra(Intent.EXTRA_TEXT),
    )

    private fun parseShare(intent: Intent): IncomingArticle {
        val html = intent.getStringExtra(Intent.EXTRA_HTML_TEXT)
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        val url = text?.lineSequence()?.mapNotNull { it.extractUrl() }?.firstOrNull()
        return IncomingArticle(
            title = intent.getStringExtra(Intent.EXTRA_TITLE)?.clean(),
            originalUrl = url,
            html = html,
            text = text,
        )
    }

    private fun String.extractUrl(): String? = split(Regex("\\s+")).firstOrNull { token ->
        runCatching {
            val uri = Uri.parse(token.trim())
            uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }?.trimEnd('.', ',', ')')

    private fun String.clean(): String = trim().takeIf { it.isNotBlank() } ?: ""
}
