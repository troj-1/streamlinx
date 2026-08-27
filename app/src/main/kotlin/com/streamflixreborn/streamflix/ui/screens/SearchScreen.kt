package com.streamflixreborn.streamflix.ui.screens
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.models.Episode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.ContentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    provider: Provider,
    onItemClick: (Item) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(query, provider) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        delay(300)
        try {
            val res = withContext(Dispatchers.IO) { provider.search(query) }
            results = res
        } catch (e: Exception) {
            // handle error
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141414))
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFE50914))
            } else if (results.isEmpty() && query.isNotBlank()) {
                Text("No results found", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { item ->
                        when (item) {
                            is Movie -> ContentCard(
                                title = (item as Movie).title,
                                posterUrl = (item as Movie).poster,
                                quality = (item as Movie).quality,
                                rating = (item as Movie).rating,
                                year = (item as Movie).released?.get(java.util.Calendar.YEAR)?.toString(),
                                onClick = { onItemClick(item) }
                            )
                            is TvShow -> ContentCard(
                                title = (item as TvShow).title,
                                posterUrl = (item as TvShow).poster,
                                quality = (item as TvShow).quality,
                                rating = (item as TvShow).rating,
                                year = (item as TvShow).released?.get(java.util.Calendar.YEAR)?.toString(),
                                onClick = { onItemClick(item) }
                            )
                            is Episode -> { }
                        }
                    }
                }
            }
        }
    }
}
