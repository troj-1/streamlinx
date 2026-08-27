package com.streamflixreborn.streamflix.player

import java.io.File

/**
 * Cross-platform mpv player launcher.
 * Auto-detects mpv installation on Windows, Linux, and macOS.
 * Can auto-install via winget on Windows or guide the user.
 */
object MpvPlayer {

    private val isWindows = System.getProperty("os.name").lowercase().contains("win")
    private val isMac = System.getProperty("os.name").lowercase().contains("mac")
    private val isLinux = !isWindows && !isMac

    /** Cached path to mpv executable */
    private var mpvPath: String? = null

    fun play(
        url: String,
        headers: Map<String, String>? = null,
        subtitleFile: String? = null
    ) {
        val mpv = findMpv() ?: throw MpvNotInstalledException(getInstallInstructions())

        val command = mutableListOf(mpv)

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
            processBuilder.start()
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to launch mpv at '$mpv'. ${getInstallInstructions()}. Error: ${e.message}",
                e
            )
        }
    }

    /** Find mpv on the system, searching common paths */
    fun findMpv(): String? {
        mpvPath?.let { if (File(it).exists()) return it }

        val candidates = mutableListOf<String>()

        if (isWindows) {
            val localAppData = System.getenv("LOCALAPPDATA") ?: ""
            val programFiles = System.getenv("ProgramFiles") ?: "C:\\Program Files"
            val programFilesX86 = System.getenv("ProgramFiles(x86)") ?: "C:\\Program Files (x86)"
            val userProfile = System.getenv("USERPROFILE") ?: ""

            candidates.addAll(listOf(
                // Winget install locations
                "$localAppData\\Microsoft\\WinGet\\Links\\mpv.exe",
                // Search winget packages dir for mpv
                *findInDir("$localAppData\\Microsoft\\WinGet\\Packages", "mpv.exe").toTypedArray(),
                // Scoop
                "$userProfile\\scoop\\shims\\mpv.exe",
                "$userProfile\\scoop\\apps\\mpv\\current\\mpv.exe",
                // Chocolatey
                "C:\\ProgramData\\chocolatey\\bin\\mpv.exe",
                // Common install paths
                "$programFiles\\mpv\\mpv.exe",
                "$programFilesX86\\mpv\\mpv.exe",
                "$localAppData\\Programs\\mpv\\mpv.exe",
                // mpv.net
                "$programFiles\\mpv.net\\mpvnet.exe",
                // Portable in project dir
                "mpv\\mpv.exe",
                // PATH
                "mpv.exe"
            ))
        } else if (isMac) {
            candidates.addAll(listOf(
                "/opt/homebrew/bin/mpv",
                "/usr/local/bin/mpv",
                "/usr/bin/mpv",
                "mpv"
            ))
        } else { // Linux
            candidates.addAll(listOf(
                "/usr/bin/mpv",
                "/usr/local/bin/mpv",
                "/snap/bin/mpv",
                "/var/lib/flatpak/exports/bin/io.mpv.Mpv",
                "mpv"
            ))
        }

        for (candidate in candidates) {
            if (candidate.contains(File.separator) || candidate.contains("/") || candidate.contains("\\")) {
                if (File(candidate).exists()) {
                    mpvPath = candidate
                    return candidate
                }
            } else {
                // Try running it to see if it's on PATH
                try {
                    val checkCmd = if (isWindows) listOf("where", candidate) else listOf("which", candidate)
                    val p = ProcessBuilder(checkCmd)
                        .redirectErrorStream(true)
                        .start()
                    val result = p.inputStream.bufferedReader().readText().trim()
                    if (p.waitFor() == 0 && result.isNotEmpty()) {
                        mpvPath = result.lines().first()
                        return mpvPath
                    }
                } catch (_: Exception) {}
            }
        }

        return null
    }

    /** Search a directory recursively for a file */
    private fun findInDir(dir: String, filename: String): List<String> {
        val d = File(dir)
        if (!d.exists()) return emptyList()
        return try {
            d.walk().maxDepth(4).filter { it.name.equals(filename, ignoreCase = true) }.map { it.absolutePath }.toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isInstalled(): Boolean = findMpv() != null

    /** Try to install mpv automatically. Returns true if successful. */
    fun tryAutoInstall(): Boolean {
        if (isWindows) {
            return try {
                println("Streamflix: mpv not found. Installing via winget...")
                val process = ProcessBuilder(
                    "winget", "install", "--id", "mpv-player.mpv-CI.MSVC",
                    "--accept-package-agreements", "--accept-source-agreements"
                ).inheritIO().start()
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    mpvPath = null // Reset cache to re-search
                    println("Streamflix: mpv installed successfully!")
                    true
                } else false
            } catch (_: Exception) { false }
        } else if (isLinux) {
            // Try common Linux package managers
            val commands = listOf(
                listOf("sudo", "apt", "install", "-y", "mpv"),
                listOf("sudo", "dnf", "install", "-y", "mpv"),
                listOf("sudo", "pacman", "-S", "--noconfirm", "mpv")
            )
            for (cmd in commands) {
                try {
                    val p = ProcessBuilder(cmd).inheritIO().start()
                    if (p.waitFor() == 0) { mpvPath = null; return true }
                } catch (_: Exception) {}
            }
        } else if (isMac) {
            return try {
                val p = ProcessBuilder("brew", "install", "mpv").inheritIO().start()
                if (p.waitFor() == 0) { mpvPath = null; true } else false
            } catch (_: Exception) { false }
        }
        return false
    }

    fun getInstallInstructions(): String {
        return when {
            isWindows -> "Install mpv: run 'winget install mpv-player.mpv-CI.MSVC' in terminal, or download from https://mpv.io"
            isMac -> "Install mpv: run 'brew install mpv' in terminal"
            else -> "Install mpv: run 'sudo apt install mpv' (Debian/Ubuntu) or 'sudo dnf install mpv' (Fedora)"
        }
    }

    class MpvNotInstalledException(message: String) : RuntimeException(message)
}
