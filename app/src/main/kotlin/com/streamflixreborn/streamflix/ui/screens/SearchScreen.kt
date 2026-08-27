package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.ContentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    provider: Provider,
    onMovieClick: (String) -> Unit,
    onTvShowClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Search") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search movies, TV shows...") },
            trailingIcon = {
                IconButton(onClick = {
                    if (query.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            try { results = withContext(Dispatchers.IO) { provider.search(query) } }
                            catch (_: Exception) { }
                            isLoading = false
                        }
                    }
                }) { Icon(Icons.Default.Search, "Search") }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(results) { item ->
                    when (item) {
                        is Movie -> ContentCard(item.title, item.poster, item.rating, item.quality) { onMovieClick(item.id) }
                        is TvShow -> ContentCard(item.title, item.poster, item.rating, item.quality) { onTvShowClick(item.id) }
                    }
                }
            }
        }
    }
}
