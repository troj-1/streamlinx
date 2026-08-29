package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import com.streamflixreborn.streamflix.models.Episode
import com.streamflixreborn.streamflix.models.Season
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage
import com.streamflixreborn.streamflix.utils.WatchHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailScreen(
    tvShowId: String,
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
        providerId: String?,
        onNext: (() -> Unit)?,
        onPrev: (() -> Unit)?
    ) -> Unit,
    onTvShowClick: (String) -> Unit,
    onPersonClick: (String) -> Unit
) {
    var show by remember(tvShowId) { mutableStateOf<TvShow?>(null) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }
    var selectedSeasonIndex by remember { mutableStateOf(0) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var isLoading by remember(tvShowId) { mutableStateOf(true) }
    var isLoadingEpisodes by remember { mutableStateOf(false) }
    var error by remember(tvShowId) { mutableStateOf<String?>(null) }
    var isLoadingVideo by remember { mutableStateOf<String?>(null) }

    fun playEpisode(index: Int) {
        val s = show ?: return
        val ep = episodes.getOrNull(index) ?: return
        if (isLoadingVideo != null) return
        isLoadingVideo = ep.id
        GlobalScope.launch(Dispatchers.IO) {
            try {
                println("[Streamflix] Getting servers for episode: ${ep.title} (${ep.id})")
                val videoType = Video.Type.Episode(
                    id = ep.id,
                    number = ep.number,
                    title = ep.title,
                    poster = ep.poster,
                    overview = null,
                    tvShow = Video.Type.Episode.TvShow(
                        id = s.id,
                        title = s.title,
                        poster = s.poster,
                        banner = s.banner,
                        releaseDate = s.released?.let { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(it.time) },
                        imdbId = s.imdbId
                    ),
                    season = Video.Type.Episode.Season(
                        number = selectedSeason?.number ?: 1,
                        title = selectedSeason?.title
                    )
                )
                var servers = try {
                    provider.getServers(ep.id, videoType)
                } catch (e: Exception) {
                    emptyList()
                }
                if (servers.isEmpty()) {
                    println("[Streamflix] Provider returned 0 servers, falling back to multi-source engine...")
                    val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(provider.language)
                    servers = try { fallback.getServers(ep.id, videoType) } catch (e: Exception) { emptyList() }
                }
                println("[Streamflix] Found ${servers.size} servers")
                if (servers.isNotEmpty()) {
                    val video = try {
                        com.streamflixreborn.streamflix.utils.tryAllServers(provider, servers)
                    } catch (e: Exception) {
                        val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(provider.language)
                        val fallbackServers = fallback.getServers(ep.id, videoType)
                        com.streamflixreborn.streamflix.utils.tryAllServers(fallback, fallbackServers)
                    }
                    withContext(Dispatchers.Main) {
                        val episodeTitle = "${s.title} - S${selectedSeason?.number ?: 1}:E${ep.number} ${ep.title}"
                        val onNext: (() -> Unit)? = if (index + 1 < episodes.size) { { playEpisode(index + 1) } } else null
                        val onPrev: (() -> Unit)? = if (index - 1 >= 0) { { playEpisode(index - 1) } } else null
                        val savedProgress = WatchHistoryManager.getEntry(ep.id)?.lastPlaybackPositionMillis ?: 0L

                        onPlayVideo(
                            video.source,
                            video.headers,
                            episodeTitle,
                            ep.id,
                            ep.poster ?: s.poster,
                            true,
                            s.id,
                            selectedSeason?.number ?: 1,
                            ep.number,
                            ep.title,
                            savedProgress,
                            provider.name,
                            onNext,
                            onPrev
                        )
                    }
                } else {
                    println("[Streamflix] No servers found for episode")
                }
            } catch (e: Exception) {
                println("[Streamflix] Error getting video: ${e.message}")
            } finally {
                isLoadingVideo = null
            }
        }
    }

    LaunchedEffect(tvShowId) {
        isLoading = true
        error = null
        try {
            val result = withContext(Dispatchers.IO) { provider.getTvShow(tvShowId) }
            show = result
            if (result.seasons.isNotEmpty()) {
                val s1 = result.seasons.find { it.number == 1 } ?: result.seasons.first()
                selectedSeason = s1
                selectedSeasonIndex = result.seasons.indexOf(s1)
            }
        } catch (e: Exception) {
            error = e.message ?: "Failed to load TV show"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedSeason?.id) {
        val s = selectedSeason ?: return@LaunchedEffect
        isLoadingEpisodes = true
        try {
            val eps = withContext(Dispatchers.IO) { provider.getEpisodesBySeason(s.id) }
            episodes = eps
        } catch (e: Exception) {
            e.printStackTrace()
            episodes = emptyList()
        } finally {
            isLoadingEpisodes = false
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
            show != null -> {
                val s = show!!
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        // Banner & Header
                        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                            AsyncImage(
                                url = s.banner ?: s.poster,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = s.title
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
                            var isFav by remember(s.id) { mutableStateOf(com.streamflixreborn.streamflix.utils.FavoritesManager.isFavorite(s.id)) }
                            IconButton(
                                onClick = {
                                    val favItem = com.streamflixreborn.streamflix.utils.FavoriteItem(
                                        id = s.id,
                                        title = s.title,
                                        poster = s.poster,
                                        banner = s.banner,
                                        isTvShow = true,
                                        rating = s.rating,
                                        quality = s.quality,
                                        overview = s.overview
                                    )
                                    isFav = com.streamflixreborn.streamflix.utils.FavoritesManager.toggleFavorite(favItem)
                                },
                                modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color(0xFFE50914) else Color.White
                                )
                            }
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                            ) {
                                Text(s.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (s.rating != null && s.rating!! > 0) {
                                        Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(String.format("%.1f", s.rating), color = Color.White, fontSize = 14.sp)
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    if (!s.quality.isNullOrBlank()) {
                                        Surface(color = Color(0xFF333333), shape = RoundedCornerShape(4.dp)) {
                                            Text(s.quality!!, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    if (s.released != null) {
                                        Text("${s.released?.get(java.util.Calendar.YEAR)}", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Overview
                    if (!s.overview.isNullOrBlank()) {
                        item {
                            Text(
                                s.overview!!,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Season Selector
                    if (s.seasons.isNotEmpty()) {
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("Seasons", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    s.seasons.forEachIndexed { index, season ->
                                        val isSelected = selectedSeason?.id == season.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedSeason = season
                                                selectedSeasonIndex = index
                                            },
                                            label = {
                                                Text(
                                                    if (season.title?.isNotBlank() == true) season.title!! else "Season ${season.number}",
                                                    color = if (isSelected) Color.White else Color.Gray
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFE50914),
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFF222222),
                                                labelColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Loading episodes indicator
                    if (isLoadingEpisodes) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE50914), modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    // Episodes list
                    itemsIndexed(episodes) { index, ep ->
                        val episodeIsLoading = isLoadingVideo == ep.id
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                playEpisode(index)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val history = WatchHistoryManager.getEntry(ep.id)
                            val progress = if (history != null && history.durationMillis > 0) {
                                (history.lastPlaybackPositionMillis.toFloat() / history.durationMillis.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
                                AsyncImage(
                                    url = ep.poster ?: s.poster,
                                    modifier = Modifier.width(160.dp).height(90.dp),
                                    contentDescription = "Episode"
                                )
                                Icon(
                                    Icons.Default.PlayArrow,
                                    "Play",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(40.dp).align(Alignment.Center)
                                )
                                if (progress > 0.02f) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(4.dp)
                                            .align(Alignment.BottomCenter),
                                        color = Color(0xFFE50914),
                                        trackColor = Color.DarkGray.copy(alpha = 0.5f)
                                    )
                                }
                                if (episodeIsLoading) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFE50914),
                                        modifier = Modifier.size(32.dp).align(Alignment.Center)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "E${ep.number} - ${ep.title}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                if (ep.overview?.isNotBlank() == true) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        ep.overview!!,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    // Cast Section
                    if (s.cast.isNotEmpty()) {
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
                                items(s.cast) { person ->
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
