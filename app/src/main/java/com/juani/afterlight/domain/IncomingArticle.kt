package com.juani.afterlight.domain

data class IncomingArticle(
    val title: String? = null,
    val source: String? = null,
    val originalUrl: String? = null,
    val resolvedUrl: String? = null,
    val html: String? = null,
    val text: String? = null,
)
