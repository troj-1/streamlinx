package com.streamflixreborn.streamflix.utils

import java.io.File
import java.util.Properties

/**
 * Desktop replacement for Android SharedPreferences.
 * Stores settings in a local properties file.
 */
object UserPreferences {
    private val configFile = File(System.getProperty("user.home"), ".streamflix/config.properties")
    private val props = Properties()

    init {
        configFile.parentFile?.mkdirs()
        if (configFile.exists()) {
            configFile.inputStream().use { props.load(it) }
        }
    }

    private fun save() {
        configFile.outputStream().use { props.store(it, "Streamflix Configuration") }
    }

    // Current provider is managed at runtime, not persisted here
    var currentProvider: Any? = null

    var dohProviderUrl: String
        get() = props.getProperty("doh_url", "https://cloudflare-dns.com/dns-query").ifBlank { "https://cloudflare-dns.com/dns-query" }
        set(value) { props.setProperty("doh_url", value); save() }

    var tmdbApiKey: String
        get() = props.getProperty("tmdb_api_key", System.getenv("TMDB_API_KEY") ?: "adc5047f27e588c9347087931a696cf4")
        set(value) { props.setProperty("tmdb_api_key", value); save() }

    var subtitleLanguage: String
        get() = props.getProperty("subtitle_language", "en")
        set(value) { props.setProperty("subtitle_language", value); save() }

    var quality: String
        get() = props.getProperty("quality", "1080p")
        set(value) { props.setProperty("quality", value); save() }

    var favoriteProviders: List<String>
        get() = props.getProperty("favorite_providers", "").split(",").filter { it.isNotBlank() }
        set(value) { props.setProperty("favorite_providers", value.joinToString(",")); save() }

    var appLanguage: String
        get() = props.getProperty("app_language", "en")
        set(value) { props.setProperty("app_language", value); save() }

    var providerLanguage: String?
        get() = props.getProperty("provider_language")
        set(value) {
            if (value != null) props.setProperty("provider_language", value)
            else props.remove("provider_language")
            save()
        }

    var isNsfwEnabled: Boolean
        get() = props.getProperty("nsfw_enabled", "false").toBoolean()
        set(value) { props.setProperty("nsfw_enabled", value.toString()); save() }

    var enableTmdb: Boolean
        get() = props.getProperty("enable_tmdb", "true").toBoolean()
        set(value) { props.setProperty("enable_tmdb", value.toString()); save() }

    // Provider cache constants
    const val PROVIDER_URL = "provider_url"
    const val PROVIDER_LOGO = "provider_logo"
    const val PROVIDER_PORTAL_URL = "provider_portal_url"
    const val PROVIDER_AUTOUPDATE = "provider_autoupdate"
    const val PROVIDER_CONFIG_URL = "provider_config_url"

    fun getProviderCache(provider: Any?, key: String): String? {
        val name = provider?.javaClass?.simpleName ?: return null
        return props.getProperty("cache_${name}_$key")
    }

    fun setProviderCache(provider: Any?, key: String, value: String) {
        val name = provider?.javaClass?.simpleName ?: return
        props.setProperty("cache_${name}_$key", value)
        save()
    }
}
