package com.streamflixreborn.streamflix.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class WatchHistoryEntry(
    val id: String,
    val title: String,
    val poster: String?,
    val isTvShow: Boolean,
    val tvShowId: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    var lastPlaybackPositionMillis: Long = 0L,
    var durationMillis: Long = 0L,
    var lastEngagementTimeUtcMillis: Long = System.currentTimeMillis(),
    val providerId: String? = null
)

object WatchHistoryManager {
    private val gson = Gson()
    private val storageDir = File(System.getProperty("user.home"), ".streamflix").apply { mkdirs() }
    private val historyFile = File(storageDir, "watch_history.json")

    private val entries = mutableListOf<WatchHistoryEntry>()

    init {
        loadHistory()
    }

    @Synchronized
    fun getHistory(): List<WatchHistoryEntry> {
        return entries.sortedByDescending { it.lastEngagementTimeUtcMillis }
    }

    @Synchronized
    fun getEntry(id: String): WatchHistoryEntry? {
        return entries.find { it.id == id || it.tvShowId == id }
    }

    @Synchronized
    fun saveProgress(
        id: String,
        title: String,
        poster: String?,
        isTvShow: Boolean,
        tvShowId: String? = null,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeTitle: String? = null,
        positionMs: Long,
        durationMs: Long,
        providerId: String? = null
    ) {
        if (durationMs <= 0 || positionMs <= 0) return

        // If position is past 95% of the video, mark as completed (reset position or keep finished state)
        val existingIndex = entries.indexOfFirst { it.id == id || (isTvShow && it.tvShowId == tvShowId && it.tvShowId != null) }
        val entry = if (existingIndex >= 0) {
            entries[existingIndex].copy(
                id = id,
                title = title,
                poster = poster ?: entries[existingIndex].poster,
                isTvShow = isTvShow,
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                episodeTitle = episodeTitle,
                lastPlaybackPositionMillis = positionMs,
                durationMillis = durationMs,
                lastEngagementTimeUtcMillis = System.currentTimeMillis(),
                providerId = providerId ?: entries[existingIndex].providerId
            ).also {
                entries[existingIndex] = it
            }
        } else {
            WatchHistoryEntry(
                id = id,
                title = title,
                poster = poster,
                isTvShow = isTvShow,
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                episodeTitle = episodeTitle,
                lastPlaybackPositionMillis = positionMs,
                durationMillis = durationMs,
                lastEngagementTimeUtcMillis = System.currentTimeMillis(),
                providerId = providerId
            ).also {
                entries.add(0, it)
            }
        }

        saveHistoryToFile()
    }

    @Synchronized
    fun removeEntry(id: String) {
        entries.removeAll { it.id == id || it.tvShowId == id }
        saveHistoryToFile()
    }

    @Synchronized
    private fun loadHistory() {
        try {
            if (historyFile.exists()) {
                val json = historyFile.readText()
                val type = object : TypeToken<List<WatchHistoryEntry>>() {}.type
                val list: List<WatchHistoryEntry>? = gson.fromJson(json, type)
                if (list != null) {
                    entries.clear()
                    entries.addAll(list)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun saveHistoryToFile() {
        try {
            val json = gson.toJson(entries)
            historyFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
