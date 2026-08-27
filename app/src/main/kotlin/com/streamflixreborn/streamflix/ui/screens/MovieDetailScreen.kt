package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.extractors.Extractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    provider: Provider,
    movieId: String,
    onPlay: (String, Map<String, String>?) -> Unit,
    onBack: () -> Unit
) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var servers by remember { mutableStateOf<List<Video.Server>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingServers by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(movieId) {
        isLoading = true
        try {
            movie = withContext(Dispatchers.IO) { provider.getMovie(movieId) }
        } catch (e: Exception) { error = e.message }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(movie?.title ?: "Loading...") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
            movie != null -> {
                val m = movie!!
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
                ) {
                    // Title
                    Text(m.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    // Meta info row
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        m.rating?.let { Text("\u2B50 ${String.format("%.1f", it)}", color = MaterialTheme.colorScheme.primary) }
                        m.quality?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        m.runtime?.let { Text("${it}min", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Play button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoadingServers = true
                                try {
                                    val videoType = Video.Type.Movie(
                                        id = m.id, title = m.title,
                                        releaseDate = m.released?.let { cal -> java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.time) } ?: "",
                                        poster = m.poster ?: "", imdbId = m.imdbId
                                    )
                                    val srvs = withContext(Dispatchers.IO) { provider.getServers(m.id, videoType) }
                                    servers = srvs
                                    // Try first server
                                    if (srvs.isNotEmpty()) {
                                        val video = withContext(Dispatchers.IO) { provider.getVideo(srvs.first()) }
                                        onPlay(video.source, video.headers)
                                    }
                                } catch (e: Exception) { error = "Playback error: ${e.message}" }
                                isLoadingServers = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isLoadingServers
                    ) {
                        if (isLoadingServers) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else {
                            Icon(Icons.Default.PlayArrow, "Play")
                            Spacer(Modifier.width(8.dp))
                            Text("Play", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Server list
                    if (servers.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Servers", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        servers.forEach { server ->
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val video = withContext(Dispatchers.IO) { provider.getVideo(server) }
                                            onPlay(video.source, video.headers)
                                        } catch (e: Exception) { error = "Server error: ${e.message}" }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) { Text(server.name) }
                        }
                    }

                    // Overview
                    Spacer(Modifier.height(24.dp))
                    m.overview?.let {
                        Text("Synopsis", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                    }

                    // Genres
                    if (m.genres.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(m.genres) { genre ->
                                SuggestionChip(onClick = {}, label = { Text(genre.name) })
                            }
                        }
                    }

                    // Cast
                    if (m.cast.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Cast", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            items(m.cast) { person ->
                                AssistChip(onClick = {}, label = { Text(person.name) })
                            }
                        }
                    }
                }
            }
        }
    }
}
