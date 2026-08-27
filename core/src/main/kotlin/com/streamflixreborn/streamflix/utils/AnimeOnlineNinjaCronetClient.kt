package com.streamflixreborn.streamflix.utils

import com.streamflixreborn.streamflix.compat.Log

/**
 * Desktop stub for the AnimeOnlineNinja Cronet client.
 * The original uses Android's Cronet library. On desktop, we fall back to OkHttp.
 */
object AnimeOnlineNinjaCronetClient {
    private const val TAG = "AONCronet"

    fun get(url: String, headers: Map<String, String> = emptyMap()): String? {
        Log.d(TAG, "Cronet not available on desktop, using OkHttp for: $url")
        return try {
            val request = okhttp3.Request.Builder().url(url)
            headers.forEach { (k, v) -> request.header(k, v) }
            NetworkClient.default.newCall(request.build()).execute().body?.string()
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${e.message}")
            null
        }
    }
}
