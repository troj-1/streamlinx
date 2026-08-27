package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.models.*
import com.streamflixreborn.streamflix.providers.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailScreen(
    provider: Provider,
    tvShowId: String,
    onPlay: (String, Map<String, String>?) -> Unit,
    onBack: () -> Unit
) {
    var tvShow by remember { mutableStateOf<TvShow?>(null) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tvShowId) {
        isLoading = true
        try {
            tvShow = withContext(Dispatchers.IO) { provider.getTvShow(tvShowId) }
            tvShow?.seasons?.firstOrNull()?.let { season ->
                selectedSeason = season
                episodes = withContext(Dispatchers.IO) { provider.getEpisodesBySeason(season.id) }
            }
        } catch (e: Exception) { error = e.message }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(tvShow?.title ?: "Loading...") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Error: $error", color = MaterialTheme.colorScheme.error) }
            tvShow != null -> {
                val show = tvShow!!
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text(show.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    show.overview?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }

                    // Season tabs
                    if (show.seasons.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = show.seasons.indexOf(selectedSeason).coerceAtLeast(0),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            show.seasons.forEach { season ->
                                Tab(
                                    selected = season == selectedSeason,
                                    onClick = {
                                        selectedSeason = season
                                        scope.launch {
                                            try { episodes = withContext(Dispatchers.IO) { provider.getEpisodesBySeason(season.id) } }
                                            catch (e: Exception) { error = e.message }
                                        }
                                    },
                                    text = { Text(season.title ?: "Season ${season.number}") }
                                )
                            }
                        }
                    }

                    // Episode list
                    LazyColumn {
                        items(episodes) { episode ->
                            ListItem(
                                headlineContent = { Text("${episode.number}. ${episode.title ?: "Episode ${episode.number}"}") },
                                supportingContent = { episode.overview?.let { Text(it, maxLines = 2) } },
                                trailingContent = {
                                    IconButton(onClick = {
                                        scope.launch {
                                            try {
                                                val videoType = Video.Type.Episode(
                                                    id = episode.id, number = episode.number,
                                                    title = episode.title, poster = episode.poster, overview = episode.overview,
                                                    tvShow = Video.Type.Episode.TvShow(show.id, show.title, show.poster, show.banner, null, show.imdbId),
                                                    season = Video.Type.Episode.Season(selectedSeason?.number ?: 0, selectedSeason?.title)
                                                )
                                                val servers = withContext(Dispatchers.IO) { provider.getServers(episode.id, videoType) }
                                                if (servers.isNotEmpty()) {
                                                    val video = withContext(Dispatchers.IO) { provider.getVideo(servers.first()) }
                                                    onPlay(video.source, video.headers)
                                                }
                                            } catch (e: Exception) { error = "Play error: ${e.message}" }
                                        }
                                    }) { Icon(Icons.Default.PlayArrow, "Play") }
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}
