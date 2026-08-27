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
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.ContentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TvShowsScreen(
    provider: Provider,
    onTvShowClick: (TvShow) -> Unit
) {
    var shows by remember(provider) { mutableStateOf<List<TvShow>>(emptyList()) }
    var page by remember(provider) { mutableStateOf(1) }
    var isLoading by remember(provider) { mutableStateOf(false) }

    LaunchedEffect(provider, page) {
        isLoading = true
        try {
            val result = withContext(Dispatchers.IO) { provider.getTvShows(page) }
            shows = shows + result
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
            items(shows) { show ->
                ContentCard(
                    title = show.title,
                    posterUrl = show.poster,
                    quality = show.quality,
                    rating = show.rating,
                    year = show.released?.get(java.util.Calendar.YEAR)?.toString(),
                    onClick = { onTvShowClick(show) }
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
