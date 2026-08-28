package com.streamflixreborn.streamflix.utils

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class TMDbTest {
    @Test
    fun testTmdPopularMovies() = runBlocking {
        try {
            val res = TMDb3.MovieLists.popular(page = 1)
            println("Successfully fetched ${res.results.size} popular movies")
            assertTrue(res.results.isNotEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testTmdTrending() = runBlocking {
        try {
            val res = TMDb3.Trending.all(TMDb3.Params.TimeWindow.DAY, page = 1)
            println("Successfully fetched ${res.results.size} trending items")
            assertTrue(res.results.isNotEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
