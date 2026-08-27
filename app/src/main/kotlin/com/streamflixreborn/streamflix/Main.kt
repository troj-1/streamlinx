package com.streamflixreborn.streamflix

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.providers.TmdbProvider
import com.streamflixreborn.streamflix.ui.screens.*

sealed class Screen {
    object Home : Screen()
    object Movies : Screen()
    object TvShows : Screen()
    object Favorites : Screen()
    object Settings : Screen()
    object Providers : Screen()
    object Search : Screen()
    data class MovieDetail(val id: String) : Screen()
    data class TvShowDetail(val id: String) : Screen()
    data class Player(val url: String, val headers: Map<String, String>?) : Screen()
}

val streamflixColorScheme = darkColorScheme(
    background = Color(0xFF141414),
    surface = Color(0xFF1A1A1A),
    primary = Color(0xFFE50914),
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.White,
    secondary = Color(0xFF564d4d),
    surfaceVariant = Color(0xFF2A2A2A)
)

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1280.dp, 800.dp),
        position = WindowPosition(Alignment.Center)
    )

    var backStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val currentScreen = backStack.lastOrNull() ?: Screen.Home

    var currentProvider by remember { 
        mutableStateOf<Provider>(
            Provider.providers.keys.firstOrNull() ?: TmdbProvider("en")
        ) 
    }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack = backStack + screen
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Streamflix",
        state = windowState,
        onKeyEvent = { event ->
            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                if (backStack.size > 1) {
                    navigateBack()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    ) {
        window.minimumSize = java.awt.Dimension(900, 600)

        MaterialTheme(colorScheme = streamflixColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (currentScreen is Screen.Player) {
                    PlayerScreen(
                        videoUrl = currentScreen.url,
                        headers = currentScreen.headers,
                        onBack = { navigateBack() }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (currentScreen) {
                                is Screen.Home -> {
                                    HomeScreen(
                                        provider = currentProvider,
                                        onProviderClick = { navigateTo(Screen.Providers) },
                                        onSearchClick = { navigateTo(Screen.Search) },
                                        onItemClick = { item ->
                                            when (item) {
                                                is Movie -> navigateTo(Screen.MovieDetail(item.id))
                                                is TvShow -> navigateTo(Screen.TvShowDetail(item.id))
                                                else -> {}
                                            }
                                        },
                                        onWatchClick = { item ->
                                            when (item) {
                                                is Movie -> navigateTo(Screen.MovieDetail(item.id))
                                                is TvShow -> navigateTo(Screen.TvShowDetail(item.id))
                                                else -> {}
                                            }
                                        }
                                    )
                                }
                                is Screen.Movies -> {
                                    MoviesScreen(
                                        provider = currentProvider,
                                        onMovieClick = { movie ->
                                            navigateTo(Screen.MovieDetail(movie.id))
                                        }
                                    )
                                }
                                is Screen.TvShows -> {
                                    TvShowsScreen(
                                        provider = currentProvider,
                                        onTvShowClick = { tvShow ->
                                            navigateTo(Screen.TvShowDetail(tvShow.id))
                                        }
                                    )
                                }
                                is Screen.Favorites -> {
                                    FavoritesScreen()
                                }
                                is Screen.Settings -> {
                                    SettingsScreen(
                                        currentProvider = currentProvider,
                                        onChangeProvider = { navigateTo(Screen.Providers) }
                                    )
                                }
                                is Screen.Providers -> {
                                    ProvidersScreen(
                                        currentProvider = currentProvider,
                                        onProviderSelected = { provider ->
                                            currentProvider = provider
                                            backStack = listOf(Screen.Home)
                                        },
                                        onBack = { navigateBack() }
                                    )
                                }
                                is Screen.Search -> {
                                    SearchScreen(
                                        provider = currentProvider,
                                        onItemClick = { item ->
                                            when (item.javaClass.simpleName) {
                                                "MovieItem", "Movie" -> {
                                                    try {
                                                        val movie = item.javaClass.getMethod("getMovie").invoke(item) as Movie
                                                        navigateTo(Screen.MovieDetail(movie.id))
                                                    } catch (e: Exception) {
                                                        try {
                                                            val id = item.javaClass.getMethod("getId").invoke(item) as String
                                                            navigateTo(Screen.MovieDetail(id))
                                                        } catch (ex: Exception) {}
                                                    }
                                                }
                                                "TvShowItem", "TvShow", "EpisodeItem", "Episode" -> {
                                                    try {
                                                        val tvShow = item.javaClass.getMethod("getTvShow").invoke(item) as TvShow
                                                        navigateTo(Screen.TvShowDetail(tvShow.id))
                                                    } catch (e: Exception) {
                                                        try {
                                                            val id = item.javaClass.getMethod("getId").invoke(item) as String
                                                            navigateTo(Screen.TvShowDetail(id))
                                                        } catch (ex: Exception) {}
                                                    }
                                                }
                                            }
                                        },
                                        onBack = { navigateBack() }
                                    )
                                }
                                is Screen.MovieDetail -> {
                                    MovieDetailScreen(
                                        movieId = currentScreen.id,
                                        provider = currentProvider,
                                        onBack = { navigateBack() },
                                        onPlayVideo = { url, headers ->
                                            navigateTo(Screen.Player(url, headers))
                                        },
                                        onMovieClick = { id ->
                                            navigateTo(Screen.MovieDetail(id))
                                        },
                                        onPersonClick = { personId ->
                                            // Optional handler
                                        }
                                    )
                                }
                                is Screen.TvShowDetail -> {
                                    TvShowDetailScreen(
                                        tvShowId = currentScreen.id,
                                        provider = currentProvider,
                                        onBack = { navigateBack() },
                                        onPlayVideo = { url, headers ->
                                            navigateTo(Screen.Player(url, headers))
                                        },
                                        onTvShowClick = { id ->
                                            navigateTo(Screen.TvShowDetail(id))
                                        },
                                        onPersonClick = { personId ->
                                            // Optional handler
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }

                        val isMainTab = currentScreen is Screen.Home || 
                                        currentScreen is Screen.Movies || 
                                        currentScreen is Screen.TvShows || 
                                        currentScreen is Screen.Favorites || 
                                        currentScreen is Screen.Settings

                        if (isMainTab) {
                            val support = Provider.providers[currentProvider]
                            val supportsMovies = support?.movies ?: true
                            val supportsTvShows = support?.tvShows ?: true

                            NavigationBar(
                                containerColor = Color(0xFF1A1A1A),
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Home,
                                    onClick = {
                                        backStack = listOf(Screen.Home)
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color.LightGray,
                                        unselectedTextColor = Color.LightGray,
                                        indicatorColor = Color.Transparent
                                    )
                                )

                                if (supportsMovies) {
                                    NavigationBarItem(
                                        selected = currentScreen is Screen.Movies,
                                        onClick = {
                                            if (currentScreen !is Screen.Movies) {
                                                backStack = listOf(Screen.Home, Screen.Movies)
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Movie, contentDescription = "Movies") },
                                        label = { Text("Movies") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = Color.LightGray,
                                            unselectedTextColor = Color.LightGray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }

                                if (supportsTvShows) {
                                    NavigationBarItem(
                                        selected = currentScreen is Screen.TvShows,
                                        onClick = {
                                            if (currentScreen !is Screen.TvShows) {
                                                backStack = listOf(Screen.Home, Screen.TvShows)
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Tv, contentDescription = "TV Shows") },
                                        label = { Text("TV Shows") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = Color.LightGray,
                                            unselectedTextColor = Color.LightGray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Favorites,
                                    onClick = {
                                        if (currentScreen !is Screen.Favorites) {
                                            backStack = listOf(Screen.Home, Screen.Favorites)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                                    label = { Text("Favorites") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color.LightGray,
                                        unselectedTextColor = Color.LightGray,
                                        indicatorColor = Color.Transparent
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Settings,
                                    onClick = {
                                        if (currentScreen !is Screen.Settings) {
                                            backStack = listOf(Screen.Home, Screen.Settings)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color.LightGray,
                                        unselectedTextColor = Color.LightGray,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
