package com.streamflixreborn.streamflix.utils

import com.google.gson.Gson
import java.io.File

data class SettingsData(
    var appLanguage: String = "en", // en, de, ru, es, fr, it, pl
    var autoplayNextEpisode: Boolean = true,
    var autoSkipIntro: Boolean = false,
    var defaultSpeed: Float = 1.0f,
    var subtitleSize: String = "Medium", // Small, Medium, Large
    var audioLanguage: String = "Default",
    var subtitleLanguage: String = "Default"
)

object AppSettings {
    private val gson = Gson()
    private val storageDir = File(System.getProperty("user.home"), ".streamflix").apply { mkdirs() }
    private val settingsFile = File(storageDir, "settings.json")

    var data = SettingsData()
        private set

    init {
        loadSettings()
    }

    @Synchronized
    fun update(block: SettingsData.() -> Unit) {
        data.block()
        saveSettings()
    }

    @Synchronized
    private fun loadSettings() {
        try {
            if (settingsFile.exists()) {
                val json = settingsFile.readText()
                val loaded = gson.fromJson(json, SettingsData::class.java)
                if (loaded != null) {
                    data = loaded
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun saveSettings() {
        try {
            val json = gson.toJson(data)
            settingsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
