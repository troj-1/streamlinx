package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.models.Category
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage
import com.streamflixreborn.streamflix.ui.components.CategoryRow
import com.streamflixreborn.streamflix.ui.components.FeaturedCarousel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    provider: Provider,
    onProviderClick: () -> Unit,
    onSearchClick: () -> Unit,
    onItemClick: (Item) -> Unit,
    onWatchClick: (Item) -> Unit
) {
    var categories by remember(provider) { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember(provider) { mutableStateOf(true) }
    var error by remember(provider) { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(provider) {
        isLoading = true
        error = null
        try {
            val result = withContext(Dispatchers.IO) { provider.getHome() }
            categories = result
        } catch (e: Exception) {
            error = e.message ?: "An error occurred"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp).clickable { onProviderClick() }) {
                        AsyncImage(
                            url = provider.logo,
                            modifier = Modifier.size(40.dp),
                            contentDescription = "Provider Logo"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: $error", color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                error = null
                                try {
                                    val result = withContext(Dispatchers.IO) { provider.getHome() }
                                    categories = result
                                } catch (e: Exception) {
                                    error = e.message ?: "An error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val featured = categories.firstOrNull { it.name.isBlank() }
                    val remaining = categories.filter { it.name.isNotBlank() }

                    if (featured != null) {
                        item {
                            FeaturedCarousel(
                                items = featured.list,
                                onItemClick = onItemClick,
                                onWatchClick = onWatchClick
                            )
                        }
                    }

                    items(remaining) { category ->
                        CategoryRow(
                            title = category.name,
                            items = category.list,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
        }
    }
}
