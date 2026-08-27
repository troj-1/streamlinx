package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.ContentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MoviesScreen(
    provider: Provider,
    onMovieClick: (Movie) -> Unit
) {
    var movies by remember(provider) { mutableStateOf<List<Movie>>(emptyList()) }
    var page by remember(provider) { mutableStateOf(1) }
    var isLoading by remember(provider) { mutableStateOf(false) }
    
    LaunchedEffect(provider, page) {
        isLoading = true
        try {
            val result = withContext(Dispatchers.IO) { provider.getMovies(page) }
            movies = movies + result
        } catch (e: Exception) {
            // handle error
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies) { movie ->
                ContentCard(
                    title = movie.title,
                    posterUrl = movie.poster,
                    quality = movie.quality,
                    rating = movie.rating,
                    year = movie.released?.get(java.util.Calendar.YEAR)?.toString(),
                    onClick = { onMovieClick(movie) }
                )
            }
            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp).wrapContentWidth(Alignment.CenterHorizontally),
                        color = Color(0xFFE50914)
                    )
                }
            }
        }
    }
}
