package com.streamflixreborn.streamflix.providers

import com.streamflixreborn.streamflix.models.Video
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class ProvidersLanguagesTest {

    @Test
    fun testAllLanguagesHome() = runBlocking {
        val languages = listOf("en", "ru", "es", "fr", "it", "de")
        for (lang in languages) {
            val provider = TmdbProvider(lang)
            val home = provider.getHome()
            println("Language $lang: Loaded ${home.size} home categories")
            assertTrue(home.isNotEmpty(), "Home should not be empty for $lang")
            
            val firstCategory = home.firstOrNull()
            assertTrue(firstCategory != null && firstCategory.list.isNotEmpty(), "List items should not be empty for $lang")
        }
    }

    @Test
    fun testMovieDetailsAndServers() = runBlocking {
        val languages = listOf("en", "es", "ru", "fr", "it", "de")
        val movieId = "10637"
        
        for (lang in languages) {
            val provider = TmdbProvider(lang)
            val movie = provider.getMovie(movieId)
            println("Language $lang: Movie title='${movie.title}', overview='${movie.overview.orEmpty().take(30)}...'")
            assertTrue(movie.title.isNotBlank())
            
            val videoType = Video.Type.Movie(
                id = movie.id,
                title = movie.title,
                releaseDate = movie.released?.let { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(it.time) }.orEmpty(),
                poster = movie.poster.orEmpty(),
                imdbId = movie.imdbId
            )
            val servers = provider.getServers(movie.id, videoType)
            println("Language $lang: Found ${servers.size} servers")
            assertTrue(servers.isNotEmpty(), "Should find servers for movie in $lang")
        }
    }

    @Test
    fun testTvShowServers() = runBlocking {
        val languages = listOf("en", "es", "ru", "fr", "it", "de")
        val tvId = "2604" // The Boondocks
        
        for (lang in languages) {
            val provider = TmdbProvider(lang)
            val show = provider.getTvShow(tvId)
            println("Language $lang: TV Show title='${show.title}'")
            assertTrue(show.title.isNotBlank())
            
            val videoType = Video.Type.Episode(
                id = "$tvId-1-1",
                number = 1,
                title = "The Garden Party",
                poster = show.poster,
                overview = null,
                tvShow = Video.Type.Episode.TvShow(
                    id = show.id,
                    title = show.title,
                    poster = show.poster,
                    banner = show.banner,
                    releaseDate = show.released?.let { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(it.time) },
                    imdbId = show.imdbId
                ),
                season = Video.Type.Episode.Season(
                    number = 1,
                    title = "Season 1"
                )
            )
            val servers = provider.getServers(videoType.id, videoType)
            println("Language $lang: Found ${servers.size} servers for TV episode")
            assertTrue(servers.isNotEmpty(), "Should find servers for TV show in $lang")
        }
    }
}
