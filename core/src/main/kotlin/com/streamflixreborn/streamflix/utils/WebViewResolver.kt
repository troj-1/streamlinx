package com.streamflixreborn.streamflix.utils

import com.streamflixreborn.streamflix.compat.Log

/**
 * Desktop stub for WebViewResolver.
 * On Android this uses a WebView to bypass Cloudflare challenges.
 * On desktop, we log a warning — users can manually solve captchas in a browser.
 */
object WebViewResolver {
    private const val TAG = "WebViewResolver"

    suspend fun resolve(url: String, headers: Map<String, String> = emptyMap<String, String>()): String? {
        Log.w(TAG, "WebViewResolver not available on desktop. URL: $url")
        Log.w(TAG, "Some providers may not work without Cloudflare bypass support.")
        return null
    }
}
