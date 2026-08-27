package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage
import com.streamflixreborn.streamflix.ui.components.ContentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    provider: Provider,
    onBack: () -> Unit,
    onPlayVideo: (videoUrl: String, headers: Map<String, String>?) -> Unit,
    onMovieClick: (String) -> Unit,
    onPersonClick: (String) -> Unit
) {
    var movie by remember(movieId) { mutableStateOf<Movie?>(null) }
    var isLoading by remember(movieId) { mutableStateOf(true) }

    LaunchedEffect(movieId) {
        isLoading = true
        try {
            movie = withContext(Dispatchers.IO) { provider.getMovie(movieId) }
        } catch (e: Exception) {
            // handle error
        } finally {
            isLoading = false
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
            movie?.let { m ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    item {
                        AsyncImage(
                            url = m.banner ?: m.poster,
                            modifier = Modifier.fillMaxWidth().height(350.dp),
                            contentDescription = "Banner"
                        )
                    }
                    item {
                        Row(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                url = m.poster,
                                modifier = Modifier.width(120.dp).height(180.dp),
                                contentDescription = "Poster"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(m.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    m.rating?.let { Text("★ $it", color = Color.Yellow) }
                                    Spacer(Modifier.width(8.dp))
                                    m.quality?.let { Text(it, color = Color.White) }
                                }
                                m.genres?.let {
                                    Text(it.joinToString(", "), color = Color.Gray, fontSize = 14.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { 
                                        onPlayVideo("dummy_url", null) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                                ) {
                                    Text("Watch Now")
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            text = m.overview ?: "",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    m.cast?.let { castList ->
                        item {
                            Text("Cast", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                                items(castList) { person ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
                                        AsyncImage(
                                            url = person.image,
                                            modifier = Modifier.size(60.dp).clip(CircleShape),
                                            contentDescription = person.name
                                        )
                                        Text(person.name, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    if (m.recommendations.isNotEmpty()) {
                        item {
                            Text("Recommendations", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                                items(m.recommendations) { rec ->
                                    val recMovie = rec as? Movie
                                    ContentCard(
                                        title = recMovie?.title ?: "",
                                        posterUrl = recMovie?.poster,
                                        quality = recMovie?.quality,
                                        rating = recMovie?.rating,
                                        year = recMovie?.released?.get(java.util.Calendar.YEAR)?.toString(),
                                        onClick = { recMovie?.id?.let { onMovieClick(it) } }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
