package com.streamflixreborn.streamflix.utils

/**
 * Desktop replacement for the JNI-based key store.
 * Reads from environment variables or config file.
 */
object Keys {
    fun getUprotMsfiApiBase(): String = System.getenv("UPROT_MSFI_API_BASE") ?: ""
    fun getUprotMseApiBase(): String = System.getenv("UPROT_MSE_API_BASE") ?: ""
    fun getUprotApiKey(): String = System.getenv("UPROT_API_KEY") ?: ""
}
