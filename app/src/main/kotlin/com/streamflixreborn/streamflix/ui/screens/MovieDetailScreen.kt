package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage
import com.streamflixreborn.streamflix.ui.components.ContentCard
import com.streamflixreborn.streamflix.utils.WatchHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    provider: Provider,
    onBack: () -> Unit,
    onPlayVideo: (
        videoUrl: String,
        headers: Map<String, String>?,
        title: String?,
        itemId: String?,
        poster: String?,
        isTvShow: Boolean,
        tvShowId: String?,
        seasonNumber: Int?,
        episodeNumber: Int?,
        episodeTitle: String?,
        startPositionMs: Long?,
        providerId: String?
    ) -> Unit,
    onMovieClick: (String) -> Unit,
    onPersonClick: (String) -> Unit
) {
    var movie by remember(movieId) { mutableStateOf<Movie?>(null) }
    var isLoading by remember(movieId) { mutableStateOf(true) }
    var error by remember(movieId) { mutableStateOf<String?>(null) }
    var isLoadingVideo by remember { mutableStateOf(false) }
    var videoError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(movieId) {
        isLoading = true
        error = null
        try {
            val result = withContext(Dispatchers.IO) { provider.getMovie(movieId) }
            movie = result
        } catch (e: Exception) {
            error = e.message ?: "Failed to load movie details"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141414))) {
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: $error", color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))) {
                            Text("Go Back")
                        }
                    }
                }
            }
            movie != null -> {
                val m = movie!!
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        // Banner & Header
                        Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                            AsyncImage(
                                url = m.banner ?: m.poster,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = m.title
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC141414), Color(0xFF141414))
                                    )
                                )
                            )
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                            var isFav by remember(m.id) { mutableStateOf(com.streamflixreborn.streamflix.utils.FavoritesManager.isFavorite(m.id)) }
                            IconButton(
                                onClick = {
                                    val favItem = com.streamflixreborn.streamflix.utils.FavoriteItem(
                                        id = m.id,
                                        title = m.title,
                                        poster = m.poster,
                                        banner = m.banner,
                                        isTvShow = false,
                                        rating = m.rating,
                                        quality = m.quality,
                                        overview = m.overview
                                    )
                                    isFav = com.streamflixreborn.streamflix.utils.FavoritesManager.toggleFavorite(favItem)
                                },
                                modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    if (isFav) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color(0xFFE50914) else Color.White
                                )
                            }
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                            ) {
                                Text(m.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (m.rating != null && m.rating!! > 0) {
                                        Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(String.format("%.1f", m.rating), color = Color.White, fontSize = 14.sp)
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    if (!m.quality.isNullOrBlank()) {
                                        Surface(color = Color(0xFF333333), shape = RoundedCornerShape(4.dp)) {
                                            Text(m.quality!!, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    if (m.released != null) {
                                        Text("${m.released?.get(java.util.Calendar.YEAR)}", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))

                                // Play Button
                                if (videoError != null) {
                                    Text(videoError!!, color = Color(0xFFCF6679), fontSize = 12.sp)
                                    Spacer(Modifier.height(8.dp))
                                }
                                Button(
                                    onClick = {
                                        if (isLoadingVideo) return@Button
                                        isLoadingVideo = true
                                        videoError = null
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                println("[Streamflix] Getting servers for movie: ${m.title} (${m.id})")
                                                val videoType = Video.Type.Movie(
                                                    id = m.id,
                                                    title = m.title,
                                                    releaseDate = "",
                                                    poster = m.poster ?: "",
                                                    imdbId = m.imdbId
                                                )
                                                var servers = try {
                                                    provider.getServers(m.id, videoType)
                                                } catch (e: Exception) {
                                                    emptyList()
                                                }
                                                if (servers.isEmpty()) {
                                                    println("[Streamflix] Provider returned 0 servers, falling back to multi-source engine...")
                                                    val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(provider.language)
                                                    servers = try { fallback.getServers(m.id, videoType) } catch (e: Exception) { emptyList() }
                                                }
                                                println("[Streamflix] Found ${servers.size} servers")
                                                if (servers.isNotEmpty()) {
                                                    val video = try {
                                                        com.streamflixreborn.streamflix.utils.tryAllServers(provider, servers)
                                                    } catch (e: Exception) {
                                                        val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(provider.language)
                                                        val fallbackServers = fallback.getServers(m.id, videoType)
                                                        com.streamflixreborn.streamflix.utils.tryAllServers(fallback, fallbackServers)
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        val savedProgress = WatchHistoryManager.getEntry(m.id)?.lastPlaybackPositionMillis ?: 0L
                                                        onPlayVideo(
                                                            video.source,
                                                            video.headers,
                                                            m.title,
                                                            m.id,
                                                            m.poster,
                                                            false,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            savedProgress,
                                                            provider.name
                                                        )
                                                    }
                                                } else {
                                                    withContext(Dispatchers.Main) {
                                                        videoError = "No video sources found"
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                println("[Streamflix] Error: ${e.message}")
                                                withContext(Dispatchers.Main) {
                                                    videoError = "Error: ${e.message}"
                                                }
                                            } finally {
                                                isLoadingVideo = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isLoadingVideo) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Finding stream...")
                                    } else {
                                        Icon(Icons.Default.PlayArrow, "Play")
                                        Spacer(Modifier.width(8.dp))
                                        val saved = WatchHistoryManager.getEntry(m.id)
                                        Text(if (saved != null && saved.lastPlaybackPositionMillis > 5000) "Resume" else "Watch Now")
                                    }
                                }
                            }
                        }
                    }

                    // Overview
                    if (!m.overview.isNullOrBlank()) {
                        item {
                            Text(
                                m.overview!!,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Cast Section
                    if (m.cast.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Cast",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(m.cast) { person ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(80.dp).clickable { onPersonClick(person.name) }
                                    ) {
                                        AsyncImage(
                                            url = person.image,
                                            modifier = Modifier.size(70.dp).clip(CircleShape),
                                            contentDescription = person.name
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(person.name, fontSize = 12.sp, color = Color.White, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
