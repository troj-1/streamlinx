package com.streamflixreborn.streamflix.utils

import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.providers.Provider

/**
 * Try all available servers until one returns a valid video URL.
 * Returns the first successful Video, or throws if all servers fail.
 */
suspend fun tryAllServers(
    provider: Provider,
    servers: List<Video.Server>,
    onProgress: ((serverName: String, index: Int, total: Int) -> Unit)? = null
): Video {
    val errors = mutableListOf<String>()
    
    for ((index, server) in servers.withIndex()) {
        val serverName = server.name ?: "Server ${index + 1}"
        onProgress?.invoke(serverName, index, servers.size)
        println("[Streamflix] Trying server ${index + 1}/${servers.size}: $serverName")
        
        try {
            val video = try {
                provider.getVideo(server)
            } catch (e: Exception) {
                if (server.src.isNotBlank() && (server.src.startsWith("http") || server.src.startsWith("//"))) {
                    val fullSrc = if (server.src.startsWith("//")) "https:${server.src}" else server.src
                    com.streamflixreborn.streamflix.extractors.Extractor.extract(fullSrc, server)
                } else throw e
            }

            if (video.source.isNotBlank() && 
                !video.source.startsWith("dummy") && 
                !video.source.contains("error", ignoreCase = true)) {
                println("[Streamflix] ✓ Server '$serverName' returned: ${video.source.take(100)}")
                return video
            } else {
                println("[Streamflix] ✗ Server '$serverName' returned empty/invalid source")
                errors.add("$serverName: empty source")
            }
        } catch (e: Exception) {
            println("[Streamflix] ✗ Server '$serverName' failed: ${e.message}")
            errors.add("$serverName: ${e.message}")
        }
    }
    
    throw Exception("All ${servers.size} servers failed:\n${errors.take(5).joinToString("\n")}")
}
