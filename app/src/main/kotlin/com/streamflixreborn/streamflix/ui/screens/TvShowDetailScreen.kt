package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.models.Episode
import com.streamflixreborn.streamflix.models.Season
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailScreen(
    tvShowId: String,
    provider: Provider,
    onBack: () -> Unit,
    onPlayVideo: (videoUrl: String, headers: Map<String, String>?) -> Unit,
    onTvShowClick: (String) -> Unit,
    onPersonClick: (String) -> Unit
) {
    var show by remember(tvShowId) { mutableStateOf<TvShow?>(null) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var isLoading by remember(tvShowId) { mutableStateOf(true) }

    LaunchedEffect(tvShowId) {
        isLoading = true
        try {
            val result = withContext(Dispatchers.IO) { provider.getTvShow(tvShowId) }
            show = result
            selectedSeason = result.seasons?.firstOrNull()
        } catch (e: Exception) { }
        finally { isLoading = false }
    }

    LaunchedEffect(selectedSeason) {
        selectedSeason?.let { season ->
            try {
                episodes = withContext(Dispatchers.IO) { provider.getEpisodesBySeason(season.id ?: "") }
            } catch (e: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        } else {
            show?.let { s ->
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    item {
                        AsyncImage(
                            url = s.banner ?: s.poster,
                            modifier = Modifier.fillMaxWidth().height(350.dp),
                            contentDescription = "Banner"
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(s.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(s.overview ?: "", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }
                    
                    s.seasons?.let { seasons ->
                        item {
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.padding(16.dp)) {
                                Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))) {
                                    Text(selectedSeason?.title ?: "Select Season")
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    seasons.forEach { season ->
                                        DropdownMenuItem(
                                            text = { Text(season.title ?: "Season ${season.number}") },
                                            onClick = {
                                                selectedSeason = season
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(episodes) { ep ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayVideo("dummy", null) }
                            .padding(16.dp)
                        ) {
                            AsyncImage(
                                url = ep.poster ?: s.poster,
                                modifier = Modifier.width(120.dp).height(80.dp),
                                contentDescription = "Episode Poster"
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("${ep.number}. ${ep.title}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
