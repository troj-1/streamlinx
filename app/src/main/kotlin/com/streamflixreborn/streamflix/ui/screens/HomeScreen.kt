package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.models.Category
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.ContentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    provider: Provider,
    onMovieClick: (String) -> Unit,
    onTvShowClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackToProviders: () -> Unit
) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(provider) {
        isLoading = true
        error = null
        try {
            val home = withContext(Dispatchers.IO) { provider.getHome() }
            categories = home
        } catch (e: Exception) {
            error = e.message ?: "Failed to load content"
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "\uD83C\uDFAC ${provider.name}",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, "Search")
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Settings")
                }
                IconButton(onClick = onBackToProviders) {
                    Icon(Icons.Default.SwapHoriz, "Change Provider")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading content...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\u26A0\uFE0F Error", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch {
                                isLoading = true; error = null
                                try { categories = withContext(Dispatchers.IO) { provider.getHome() } }
                                catch (e: Exception) { error = e.message }
                                isLoading = false
                            }
                        }) { Text("Retry") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(categories) { category ->
                        CategoryRow(
                            category = category,
                            onMovieClick = onMovieClick,
                            onTvShowClick = onTvShowClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onMovieClick: (String) -> Unit,
    onTvShowClick: (String) -> Unit
) {
    if (category.name.isNotEmpty()) {
        Text(
            text = category.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(category.list.filterIsInstance<com.streamflixreborn.streamflix.compat.Item>()) { item ->
            when (item) {
                is Movie -> ContentCard(
                    title = item.title,
                    posterUrl = item.poster,
                    rating = item.rating,
                    quality = item.quality,
                    onClick = { onMovieClick(item.id) }
                )
                is TvShow -> ContentCard(
                    title = item.title,
                    posterUrl = item.poster,
                    rating = item.rating,
                    quality = item.quality,
                    onClick = { onTvShowClick(item.id) }
                )
            }
        }
    }
}
