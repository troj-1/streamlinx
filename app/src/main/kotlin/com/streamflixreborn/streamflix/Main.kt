package com.streamflixreborn.streamflix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.streamflixreborn.streamflix.ui.screens.*
import com.streamflixreborn.streamflix.ui.theme.StreamflixTheme
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.providers.SflixProvider

fun main() = application {
    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Streamflix",
    ) {
        StreamflixTheme {
            App()
        }
    }
}

enum class Screen {
    PROVIDER_SELECTION,
    HOME,
    MOVIE_DETAIL,
    TV_SHOW_DETAIL,
    SEARCH,
    PLAYER,
    SETTINGS
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.PROVIDER_SELECTION) }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var selectedMovieId by remember { mutableStateOf<String?>(null) }
    var selectedTvShowId by remember { mutableStateOf<String?>(null) }
    var playbackUrl by remember { mutableStateOf<String?>(null) }
    var playbackHeaders by remember { mutableStateOf<Map<String, String>?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            Screen.PROVIDER_SELECTION -> ProviderSelectionScreen(
                onProviderSelected = { provider ->
                    selectedProvider = provider
                    currentScreen = Screen.HOME
                }
            )
            Screen.HOME -> HomeScreen(
                provider = selectedProvider!!,
                onMovieClick = { movieId ->
                    selectedMovieId = movieId
                    currentScreen = Screen.MOVIE_DETAIL
                },
                onTvShowClick = { tvShowId ->
                    selectedTvShowId = tvShowId
                    currentScreen = Screen.TV_SHOW_DETAIL
                },
                onSearchClick = { currentScreen = Screen.SEARCH },
                onSettingsClick = { currentScreen = Screen.SETTINGS },
                onBackToProviders = { currentScreen = Screen.PROVIDER_SELECTION }
            )
            Screen.MOVIE_DETAIL -> MovieDetailScreen(
                provider = selectedProvider!!,
                movieId = selectedMovieId!!,
                onPlay = { url, headers ->
                    playbackUrl = url
                    playbackHeaders = headers
                    currentScreen = Screen.PLAYER
                },
                onBack = { currentScreen = Screen.HOME }
            )
            Screen.TV_SHOW_DETAIL -> TvShowDetailScreen(
                provider = selectedProvider!!,
                tvShowId = selectedTvShowId!!,
                onPlay = { url, headers ->
                    playbackUrl = url
                    playbackHeaders = headers
                    currentScreen = Screen.PLAYER
                },
                onBack = { currentScreen = Screen.HOME }
            )
            Screen.SEARCH -> SearchScreen(
                provider = selectedProvider!!,
                onMovieClick = { movieId ->
                    selectedMovieId = movieId
                    currentScreen = Screen.MOVIE_DETAIL
                },
                onTvShowClick = { tvShowId ->
                    selectedTvShowId = tvShowId
                    currentScreen = Screen.TV_SHOW_DETAIL
                },
                onBack = { currentScreen = Screen.HOME }
            )
            Screen.PLAYER -> PlayerScreen(
                videoUrl = playbackUrl!!,
                headers = playbackHeaders,
                onBack = { currentScreen = Screen.HOME }
            )
            Screen.SETTINGS -> SettingsScreen(
                onBack = { currentScreen = Screen.HOME }
            )
        }
    }
}
