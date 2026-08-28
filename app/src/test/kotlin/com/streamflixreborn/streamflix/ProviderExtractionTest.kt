package com.streamflixreborn.streamflix

import com.streamflixreborn.streamflix.models.*
import com.streamflixreborn.streamflix.providers.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class ProviderExtractionTest {

    @Test
    fun testRussianExtraction() = runBlocking {
        println("=== TESTING RUSSIAN TMDB EXTRACTION ===")
        val provider = TmdbProvider("ru")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Эпизод 1",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "5920",
                title = "Менталист",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt1196946"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Сезон 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} Russian servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "Russian servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name} (${server.src})")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one Russian server must resolve a valid stream")
    }

    @Test
    fun testEnglishExtraction() = runBlocking {
        println("=== TESTING ENGLISH TMDB EXTRACTION ===")
        val provider = TmdbProvider("en")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Pilot",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "5920",
                title = "The Mentalist",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt1196946"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Season 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} English servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "English servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name}")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one English server must resolve a valid stream")
    }

    @Test
    fun testGermanExtraction() = runBlocking {
        println("=== TESTING GERMAN TMDB EXTRACTION ===")
        val provider = TmdbProvider("de")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Pilot",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "1396",
                title = "Breaking Bad",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt0903747"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Staffel 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} German servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "German servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name}")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one German server must resolve a valid stream")
    }

    @Test
    fun testSpanishExtraction() = runBlocking {
        println("=== TESTING SPANISH TMDB EXTRACTION ===")
        val provider = TmdbProvider("es")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Piloto",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "5920",
                title = "El mentalista",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt1196946"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Temporada 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} Spanish servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "Spanish servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name}")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one Spanish server must resolve a valid stream")
    }

    @Test
    fun testItalianExtraction() = runBlocking {
        println("=== TESTING ITALIAN TMDB EXTRACTION ===")
        val provider = TmdbProvider("it")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Pilota",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "5920",
                title = "The Mentalist",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt1196946"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Stagione 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} Italian servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "Italian servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name}")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one Italian server must resolve a valid stream")
    }

    @Test
    fun testFrenchExtraction() = runBlocking {
        println("=== TESTING FRENCH TMDB EXTRACTION ===")
        val provider = TmdbProvider("fr")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Pilote",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "5920",
                title = "Mentalist",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt1196946"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Saison 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} French servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "French servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name}")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one French server must resolve a valid stream")
    }

    @Test
    fun testPolishExtraction() = runBlocking {
        println("=== TESTING POLISH TMDB EXTRACTION ===")
        val provider = TmdbProvider("pl")
        val videoType = Video.Type.Episode(
            id = "1",
            number = 1,
            title = "Pilot",
            poster = null,
            overview = null,
            tvShow = Video.Type.Episode.TvShow(
                id = "5920",
                title = "Mentalista",
                poster = null,
                banner = null,
                releaseDate = null,
                imdbId = "tt1196946"
            ),
            season = Video.Type.Episode.Season(
                number = 1,
                title = "Sezon 1"
            )
        )

        val servers = provider.getServers("1", videoType)
        println("Found ${servers.size} Polish servers: ${servers.map { it.name }}")
        assertTrue(servers.isNotEmpty(), "Polish servers list should not be empty")

        var success = false
        for (server in servers) {
            try {
                println("Trying server: ${server.name}")
                val video = provider.getVideo(server)
                if (video.source.isNotBlank() && !video.source.startsWith("dummy")) {
                    println("SUCCESS: Resolved stream from ${server.name}: ${video.source.take(120)}")
                    success = true
                    break
                }
            } catch (e: Exception) {
                println("Server ${server.name} failed: ${e.message}")
            }
        }
        assertTrue(success, "At least one Polish server must resolve a valid stream")
    }

    @Test
    fun testAnimeWorldExtraction() = runBlocking {
        println("=== TESTING ANIMEWORLD EXTRACTION ===")
        val provider = AnimeWorldProvider
        val search = try { provider.search("Death Note", 1) } catch (e: Exception) { emptyList() }
        println("AnimeWorld search results: ${search.size}")
        if (search.isNotEmpty()) {
            val show = search.first() as? TvShow
            if (show != null) {
                val details = provider.getTvShow(show.id)
                val ep = details.seasons.firstOrNull()?.episodes?.firstOrNull()
                if (ep != null) {
                    val videoType = Video.Type.Episode(
                        id = ep.id,
                        number = ep.number,
                        title = ep.title,
                        poster = ep.poster,
                        overview = null,
                        tvShow = Video.Type.Episode.TvShow(
                            id = show.id,
                            title = show.title,
                            poster = show.poster,
                            banner = show.banner,
                            releaseDate = null,
                            imdbId = null
                        ),
                        season = Video.Type.Episode.Season(number = 1, title = "Season 1")
                    )
                    val servers = provider.getServers(ep.id, videoType)
                    println("Found ${servers.size} AnimeWorld servers: ${servers.map { it.name }}")
                    for (server in servers) {
                        try {
                            val video = provider.getVideo(server)
                            if (video.source.isNotBlank()) {
                                println("SUCCESS AnimeWorld stream: ${video.source.take(120)}")
                                break
                            }
                        } catch (e: Exception) {
                            println("AnimeWorld server ${server.name} failed: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}
