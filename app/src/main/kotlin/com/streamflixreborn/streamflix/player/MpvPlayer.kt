package com.streamflixreborn.streamflix.player

import java.io.File

/**
 * Launches mpv to play a video URL with optional HTTP headers.
 * mpv must be installed on the system (apt install mpv).
 */
object MpvPlayer {

    fun play(
        url: String,
        headers: Map<String, String>? = null,
        subtitleFile: String? = null
    ) {
        val command = mutableListOf("mpv")

        // Add HTTP headers if provided
        if (!headers.isNullOrEmpty()) {
            val headerString = headers.entries.joinToString("\r\n") { "${it.key}: ${it.value}" }
            command.add("--http-header-fields=$headerString")
        }

        // Add subtitle file if provided
        if (subtitleFile != null) {
            command.add("--sub-file=$subtitleFile")
        }

        // Common mpv options
        command.addAll(listOf(
            "--force-window=immediate",
            "--keep-open=yes",
            "--title=Streamflix",
            "--osd-level=1",
            url
        ))

        try {
            val processBuilder = ProcessBuilder(command)
            processBuilder.inheritIO()
            val process = processBuilder.start()
            // Don't wait — mpv runs independently
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to launch mpv. Make sure mpv is installed (sudo apt install mpv). Error: ${e.message}",
                e
            )
        }
    }

    fun isInstalled(): Boolean {
        return try {
            val process = ProcessBuilder("which", "mpv").start()
            process.waitFor() == 0
        } catch (_: Exception) {
            // On Windows/WSL, try 'where' as fallback
            try {
                val process = ProcessBuilder("where", "mpv").start()
                process.waitFor() == 0
            } catch (_: Exception) {
                false
            }
        }
    }
}
