package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.ui.components.ContentCard
import com.streamflixreborn.streamflix.utils.FavoriteItem
import com.streamflixreborn.streamflix.utils.FavoritesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onMovieClick: (String) -> Unit = {},
    onTvShowClick: (String) -> Unit = {}
) {
    var favorites by remember { mutableStateOf(FavoritesManager.getFavorites()) }
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Movies, 2: TV Shows

    val filteredList = remember(favorites, selectedFilter) {
        when (selectedFilter) {
            1 -> favorites.filter { !it.isTvShow }
            2 -> favorites.filter { it.isTvShow }
            else -> favorites
        }
    }

    LaunchedEffect(Unit) {
        favorites = FavoritesManager.getFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites (${favorites.size})", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Movies", "TV Shows").forEachIndexed { index, label ->
                    val isSelected = selectedFilter == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = index },
                        label = { Text(label, color = if (isSelected) Color.White else Color.Gray) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE50914),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF222222)
                        )
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "No Favorites",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No favorites saved yet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Add movies and series to your favorites to access them quickly here.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                ) {
                    items(filteredList) { item ->
                        ContentCard(
                            title = item.title,
                            posterUrl = item.poster,
                            rating = item.rating,
                            quality = item.quality,
                            onClick = {
                                if (item.isTvShow) {
                                    onTvShowClick(item.id)
                                } else {
                                    onMovieClick(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
