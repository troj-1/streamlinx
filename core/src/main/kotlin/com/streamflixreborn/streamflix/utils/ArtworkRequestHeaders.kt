package com.streamflixreborn.streamflix.utils

import okhttp3.HttpUrl
import org.json.JSONObject
import java.util.Base64

object ArtworkRequestHeaders {
    private const val FRAGMENT_KEY = "sf_headers"

    fun withHeaders(url: String?, referer: String? = null, origin: String? = null, userAgent: String? = null, accept: String? = null, cookie: String? = null): String? {
        val imageUrl = url?.takeIf { it.isNotBlank() } ?: return null
        val headers = JSONObject()
        referer?.takeIf { it.isNotBlank() }?.let { headers.put("Referer", it) }
        origin?.takeIf { it.isNotBlank() }?.let { headers.put("Origin", it) }
        userAgent?.takeIf { it.isNotBlank() }?.let { headers.put("User-Agent", it) }
        accept?.takeIf { it.isNotBlank() }?.let { headers.put("Accept", it) }
        cookie?.takeIf { it.isNotBlank() }?.let { headers.put("Cookie", it) }
        if (headers.length() == 0) return imageUrl
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(headers.toString().toByteArray(Charsets.UTF_8))
        val separator = if (imageUrl.contains("#")) "&" else "#"
        return "$imageUrl$separator$FRAGMENT_KEY=$encoded"
    }

    fun headersFor(url: HttpUrl): Map<String, String> {
        val encoded = headerPayload(url) ?: return emptyMap()
        val decoded = runCatching { String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8) }.getOrNull() ?: return emptyMap()
        val json = runCatching { JSONObject(decoded) }.getOrNull() ?: return emptyMap()
        return json.keys().asSequence().mapNotNull { key -> json.optString(key).takeIf { it.isNotBlank() }?.let { key to it } }.toMap()
    }

    fun stripHeaders(url: HttpUrl): HttpUrl {
        val fragment = url.fragment ?: return url
        val remaining = fragment.split("&").filterNot { it.startsWith("$FRAGMENT_KEY=") }.joinToString("&").takeIf { it.isNotBlank() }
        return url.newBuilder().fragment(remaining).build()
    }

    private fun headerPayload(url: HttpUrl): String? = url.fragment?.split("&")?.firstOrNull { it.startsWith("$FRAGMENT_KEY=") }?.substringAfter("=")?.takeIf { it.isNotBlank() }
}
