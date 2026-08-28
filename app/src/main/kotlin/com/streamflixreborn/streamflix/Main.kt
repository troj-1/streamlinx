package com.streamflixreborn.streamflix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.streamflixreborn.streamflix.models.Episode
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.Season
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.providers.TmdbProvider
import com.streamflixreborn.streamflix.ui.screens.*
import com.streamflixreborn.streamflix.utils.WatchHistoryEntry
import com.streamflixreborn.streamflix.utils.WatchHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    data class Player(
        val url: String,
        val headers: Map<String, String>?,
        val title: String? = null,
        val itemId: String? = null,
        val poster: String? = null,
        val isTvShow: Boolean = false,
        val tvShowId: String? = null,
        val seasonNumber: Int? = null,
        val episodeNumber: Int? = null,
        val episodeTitle: String? = null,
        val startPositionMs: Long? = 0L,
        val providerId: String? = null,
        val providerLanguage: String? = null,
        val onNextEpisode: (() -> Unit)? = null,
        val onPreviousEpisode: (() -> Unit)? = null
    ) : Screen()
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

fun main() {
    // Initialize bundled VLC native libraries before anything else
    initBundledVlc()
    
    application {
    val windowState = rememberWindowState(
        size = DpSize(1280.dp, 800.dp),
        position = WindowPosition(Alignment.Center)
    )

    var backStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val currentScreen = backStack.lastOrNull() ?: Screen.Home

    var currentProvider by remember { 
        val initial = TmdbProvider("en")
        com.streamflixreborn.streamflix.utils.UserPreferences.providerLanguage = initial.language
        com.streamflixreborn.streamflix.utils.UserPreferences.currentProvider = initial
        mutableStateOf<Provider>(initial) 
    }

    val scope = rememberCoroutineScope()
    var isResumingFromHistory by remember { mutableStateOf(false) }

    fun navigateTo(screen: Screen) {
        if (screen is Screen.Home) {
            backStack = listOf(Screen.Home)
        } else if (backStack.lastOrNull() is Screen.Player && screen is Screen.Player) {
            backStack = backStack.dropLast(1) + screen
        } else {
            backStack = backStack + screen
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    var isFullscreen by remember { mutableStateOf(false) }

    fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        windowState.placement = if (isFullscreen) {
            androidx.compose.ui.window.WindowPlacement.Fullscreen
        } else {
            androidx.compose.ui.window.WindowPlacement.Floating
        }
    }

    fun playTvEpisodeFromList(
        showId: String,
        showTitle: String,
        showPoster: String?,
        seasonNumber: Int,
        seasonTitle: String?,
        episodes: List<Episode>,
        episodeIndex: Int,
        startPositionMs: Long? = 0L
    ) {
        val ep = episodes.getOrNull(episodeIndex) ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val videoType = Video.Type.Episode(
                    id = ep.id,
                    number = ep.number,
                    title = ep.title,
                    poster = ep.poster,
                    overview = null,
                    tvShow = Video.Type.Episode.TvShow(
                        id = showId,
                        title = showTitle,
                        poster = showPoster,
                        banner = null,
                        releaseDate = null,
                        imdbId = null
                    ),
                    season = Video.Type.Episode.Season(
                        number = seasonNumber,
                        title = seasonTitle
                    )
                )
                var servers = try {
                    currentProvider.getServers(ep.id, videoType)
                } catch (e: Exception) {
                    emptyList()
                }
                if (servers.isEmpty()) {
                    println("[Streamflix] Provider returned 0 servers, falling back to multi-source engine...")
                    val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(currentProvider.language)
                    servers = try { fallback.getServers(ep.id, videoType) } catch (e: Exception) { emptyList() }
                }
                if (servers.isNotEmpty()) {
                    val video = try {
                        com.streamflixreborn.streamflix.utils.tryAllServers(currentProvider, servers)
                    } catch (e: Exception) {
                        val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(currentProvider.language)
                        val fallbackServers = fallback.getServers(ep.id, videoType)
                        com.streamflixreborn.streamflix.utils.tryAllServers(fallback, fallbackServers)
                    }
                    withContext(Dispatchers.Main) {
                        val fullTitle = "$showTitle - S${seasonNumber}:E${ep.number}${if (!ep.title.isNullOrBlank()) " ${ep.title}" else ""}"
                        val onNext: (() -> Unit)? = if (episodeIndex + 1 < episodes.size) {
                            {
                                playTvEpisodeFromList(
                                    showId, showTitle, showPoster, seasonNumber, seasonTitle, episodes, episodeIndex + 1, 0L
                                )
                            }
                        } else null
                        val onPrev: (() -> Unit)? = if (episodeIndex - 1 >= 0) {
                            {
                                playTvEpisodeFromList(
                                    showId, showTitle, showPoster, seasonNumber, seasonTitle, episodes, episodeIndex - 1, 0L
                                )
                            }
                        } else null

                        navigateTo(
                            Screen.Player(
                                url = video.source,
                                headers = video.headers,
                                title = fullTitle,
                                itemId = ep.id,
                                poster = ep.poster ?: showPoster,
                                isTvShow = true,
                                tvShowId = showId,
                                seasonNumber = seasonNumber,
                                episodeNumber = ep.number,
                                episodeTitle = ep.title,
                                startPositionMs = startPositionMs,
                                providerId = currentProvider.name,
                                providerLanguage = currentProvider.language,
                                onNextEpisode = onNext,
                                onPreviousEpisode = onPrev
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Streamflix",
        state = windowState,
        onKeyEvent = { event ->
            if (event.type == KeyEventType.KeyDown) {
                when (event.key) {
                    Key.Escape -> {
                        if (isFullscreen) {
                            toggleFullscreen()
                            true
                        } else if (backStack.size > 1) {
                            navigateBack()
                            true
                        } else false
                    }
                    Key.F11 -> {
                        toggleFullscreen()
                        true
                    }
                    else -> false
                }
            } else false
        }
    ) {
        window.minimumSize = java.awt.Dimension(900, 600)

        MaterialTheme(colorScheme = streamflixColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (currentScreen is Screen.Player) {
                    key(currentScreen.url, currentScreen.itemId) {
                        PlayerScreen(
                            videoUrl = currentScreen.url,
                            headers = currentScreen.headers,
                            title = currentScreen.title,
                            itemId = currentScreen.itemId,
                            poster = currentScreen.poster,
                            isTvShow = currentScreen.isTvShow,
                            tvShowId = currentScreen.tvShowId,
                            seasonNumber = currentScreen.seasonNumber,
                            episodeNumber = currentScreen.episodeNumber,
                            episodeTitle = currentScreen.episodeTitle,
                            startPositionMs = currentScreen.startPositionMs,
                            providerId = currentScreen.providerId,
                            providerLanguage = currentScreen.providerLanguage,
                            onNextEpisode = currentScreen.onNextEpisode,
                            onPreviousEpisode = currentScreen.onPreviousEpisode,
                            onBack = {
                                if (isFullscreen) toggleFullscreen()
                                navigateBack()
                            },
                            onToggleFullscreen = { toggleFullscreen() }
                        )
                    }
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
                                        },
                                        onContinueWatchingPlay = { entry ->
                                            if (isResumingFromHistory) return@HomeScreen
                                            isResumingFromHistory = true
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    if (entry.isTvShow && entry.tvShowId != null) {
                                                        // 1. Try current provider, 2. Fallback to TMDb provider with current language, 3. Search current provider
                                                        val show = try {
                                                            currentProvider.getTvShow(entry.tvShowId)
                                                        } catch (e: Exception) {
                                                            try {
                                                                val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(currentProvider.language)
                                                                fallback.getTvShow(entry.tvShowId)
                                                            } catch (e2: Exception) {
                                                                val results = try { currentProvider.search(entry.title, 1) } catch (_: Exception) { emptyList() }
                                                                (results.firstOrNull { it is TvShow } as? TvShow)?.let {
                                                                    try { currentProvider.getTvShow(it.id) } catch (_: Exception) { null }
                                                                }
                                                            }
                                                        }

                                                        if (show != null) {
                                                            val targetSeasonNum = entry.seasonNumber ?: 1
                                                            val season = show.seasons.find { it.number == targetSeasonNum } ?: show.seasons.firstOrNull()
                                                            if (season != null) {
                                                                val episodes = try {
                                                                    currentProvider.getEpisodesBySeason(season.id)
                                                                } catch (e: Exception) {
                                                                    val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(currentProvider.language)
                                                                    try { fallback.getEpisodesBySeason(season.id) } catch (_: Exception) { emptyList() }
                                                                }
                                                                val epIndex = episodes.indexOfFirst { it.id == entry.id || it.number == entry.episodeNumber }.coerceAtLeast(0)
                                                                playTvEpisodeFromList(
                                                                    showId = show.id,
                                                                    showTitle = show.title,
                                                                    showPoster = show.poster,
                                                                    seasonNumber = season.number,
                                                                    seasonTitle = season.title,
                                                                    episodes = episodes,
                                                                    episodeIndex = epIndex,
                                                                    startPositionMs = entry.lastPlaybackPositionMillis
                                                                )
                                                            } else {
                                                                withContext(Dispatchers.Main) {
                                                                    navigateTo(Screen.TvShowDetail(show.id))
                                                                }
                                                            }
                                                        } else {
                                                            withContext(Dispatchers.Main) {
                                                                navigateTo(Screen.TvShowDetail(entry.tvShowId))
                                                            }
                                                        }
                                                    } else {
                                                        // Movie playback with cross-provider fallback
                                                        val videoType = Video.Type.Movie(
                                                            id = entry.id,
                                                            title = entry.title,
                                                            releaseDate = "",
                                                            poster = entry.poster ?: "",
                                                            imdbId = null
                                                        )
                                                        var servers = try {
                                                            currentProvider.getServers(entry.id, videoType)
                                                        } catch (e: Exception) {
                                                            emptyList()
                                                        }
                                                        if (servers.isEmpty()) {
                                                            val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(currentProvider.language)
                                                            servers = try { fallback.getServers(entry.id, videoType) } catch (e: Exception) { emptyList() }
                                                        }
                                                        if (servers.isNotEmpty()) {
                                                            val video = try {
                                                                com.streamflixreborn.streamflix.utils.tryAllServers(currentProvider, servers)
                                                            } catch (e: Exception) {
                                                                val fallback = com.streamflixreborn.streamflix.providers.TmdbProvider(currentProvider.language)
                                                                val fallbackServers = fallback.getServers(entry.id, videoType)
                                                                com.streamflixreborn.streamflix.utils.tryAllServers(fallback, fallbackServers)
                                                            }
                                                            withContext(Dispatchers.Main) {
                                                                navigateTo(
                                                                    Screen.Player(
                                                                        url = video.source,
                                                                        headers = video.headers,
                                                                        title = entry.title,
                                                                        itemId = entry.id,
                                                                        poster = entry.poster,
                                                                        isTvShow = false,
                                                                        tvShowId = null,
                                                                        seasonNumber = null,
                                                                        episodeNumber = null,
                                                                        episodeTitle = null,
                                                                        startPositionMs = entry.lastPlaybackPositionMillis,
                                                                        providerId = currentProvider.name,
                                                                        providerLanguage = currentProvider.language
                                                                    )
                                                                )
                                                            }
                                                        } else {
                                                            withContext(Dispatchers.Main) {
                                                                navigateTo(Screen.MovieDetail(entry.id))
                                                            }
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        if (entry.isTvShow && entry.tvShowId != null) {
                                                            navigateTo(Screen.TvShowDetail(entry.tvShowId))
                                                        } else {
                                                            navigateTo(Screen.MovieDetail(entry.id))
                                                        }
                                                    }
                                                } finally {
                                                    isResumingFromHistory = false
                                                }
                                            }
                                        },
                                        onContinueWatchingDetails = { entry ->
                                            if (entry.isTvShow && entry.tvShowId != null) {
                                                navigateTo(Screen.TvShowDetail(entry.tvShowId))
                                            } else {
                                                navigateTo(Screen.MovieDetail(entry.id))
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
                                    FavoritesScreen(
                                        onMovieClick = { movieId ->
                                            navigateTo(Screen.MovieDetail(movieId))
                                        },
                                        onTvShowClick = { tvShowId ->
                                            navigateTo(Screen.TvShowDetail(tvShowId))
                                        }
                                    )
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
                                            com.streamflixreborn.streamflix.utils.UserPreferences.providerLanguage = provider.language
                                            com.streamflixreborn.streamflix.utils.UserPreferences.currentProvider = provider
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
                                        onPlayVideo = { url, headers, title, itemId, poster, isTvShow, tvShowId, seasonNum, epNum, epTitle, startPos, provId ->
                                            navigateTo(
                                                Screen.Player(
                                                    url = url,
                                                    headers = headers,
                                                    title = title,
                                                    itemId = itemId,
                                                    poster = poster,
                                                    isTvShow = isTvShow,
                                                    tvShowId = tvShowId,
                                                    seasonNumber = seasonNum,
                                                    episodeNumber = epNum,
                                                    episodeTitle = epTitle,
                                                    startPositionMs = startPos,
                                                    providerId = provId,
                                                    providerLanguage = currentProvider.language
                                                )
                                            )
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
                                        onPlayVideo = { url, headers, title, itemId, poster, isTvShow, tvShowId, seasonNum, epNum, epTitle, startPos, provId, onNext, onPrev ->
                                            navigateTo(
                                                Screen.Player(
                                                    url = url,
                                                    headers = headers,
                                                    title = title,
                                                    itemId = itemId,
                                                    poster = poster,
                                                    isTvShow = isTvShow,
                                                    tvShowId = tvShowId,
                                                    seasonNumber = seasonNum,
                                                    episodeNumber = epNum,
                                                    episodeTitle = epTitle,
                                                    startPositionMs = startPos,
                                                    providerId = provId,
                                                    providerLanguage = currentProvider.language,
                                                    onNextEpisode = onNext,
                                                    onPreviousEpisode = onPrev
                                                )
                                            )
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

                        // Bottom Navigation Bar
                        NavigationBar(
                            containerColor = Color(0xFF141414),
                            contentColor = Color.White
                        ) {
                            val navItems = listOf(
                                Triple(Screen.Home, "Home", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                                Triple(Screen.Movies, "Movies", Pair(Icons.Filled.Movie, Icons.Outlined.Movie)),
                                Triple(Screen.TvShows, "TV Shows", Pair(Icons.Filled.Tv, Icons.Outlined.Tv)),
                                Triple(Screen.Favorites, "Favorites", Pair(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)),
                                Triple(Screen.Settings, "Settings", Pair(Icons.Filled.Settings, Icons.Outlined.Settings))
                            )

                            navItems.forEach { (screen, label, icons) ->
                                val selected = currentScreen.javaClass == screen.javaClass
                                val localizedLabel = com.streamflixreborn.streamflix.utils.Strings.get(label, com.streamflixreborn.streamflix.utils.AppSettings.data.appLanguage)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(screen) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) icons.first else icons.second,
                                            contentDescription = localizedLabel
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = localizedLabel,
                                            fontSize = 12.sp,
                                            color = if (selected) Color.White else Color.Gray
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE50914),
                                        unselectedIconColor = Color.Gray,
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
}

private fun initBundledVlc() {
    try {
        val appDir = File(System.getProperty("user.dir"))
        val candidatePaths = listOf(
            File(appDir, "app/vlc-native"),
            File(appDir, "vlc-native"),
            File("c:/MacBookLinux/streamflix-linux/app/vlc-native"),
            File("c:/MacBookLinux/streamflix-linux/vlc-native"),
            File("C:\\Program Files\\VideoLAN\\VLC"),
            File("C:\\Program Files (x86)\\VideoLAN\\VLC")
        )
        
        for (dir in candidatePaths) {
            if (dir.exists()) {
                val dll = File(dir, "libvlc.dll")
                val so = File(dir, "libvlc.so")
                val dylib = File(dir, "libvlc.dylib")
                if (dll.exists() || so.exists() || dylib.exists()) {
                    System.setProperty("jna.library.path", dir.absolutePath)
                    val pluginsDir = File(dir, "plugins")
                    if (pluginsDir.exists()) {
                        System.setProperty("VLC_PLUGIN_PATH", pluginsDir.absolutePath)
                    }
                    println("[Streamflix] Using VLC from: ${dir.absolutePath}")
                    break
                }
            }
        }
    } catch (e: Exception) {
        println("[Streamflix] Failed to initialize bundled VLC: ${e.message}")
    }
}
