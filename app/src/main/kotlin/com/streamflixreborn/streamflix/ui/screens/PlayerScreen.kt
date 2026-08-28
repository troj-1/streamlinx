package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.utils.AppSettings
import com.streamflixreborn.streamflix.utils.WatchHistoryManager
import kotlinx.coroutines.*
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ColorAlphaType
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.base.TrackDescription
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.io.File
import java.nio.ByteBuffer
import java.util.Base64

class DirectVideoRenderer(
    private val onFrame: (ImageBitmap) -> Unit
) : RenderCallback, BufferFormatCallback {

    override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
        return BufferFormat("RV32", sourceWidth, sourceHeight, intArrayOf(sourceWidth * 4), intArrayOf(sourceHeight))
    }

    override fun allocatedBuffers(buffers: Array<ByteBuffer>) {}

    override fun display(mediaPlayer: MediaPlayer, nativeBuffers: Array<ByteBuffer>, bufferFormat: BufferFormat) {
        try {
            val buffer = nativeBuffers[0]
            val width = bufferFormat.width
            val height = bufferFormat.height
            val pitch = bufferFormat.pitches[0]
            val bytes = ByteArray(pitch * height)
            buffer.get(bytes)
            buffer.rewind()

            val info = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.PREMUL)
            val skia = org.jetbrains.skia.Image.makeRaster(info, bytes, pitch)
            onFrame(skia.toComposeImageBitmap())
        } catch (_: Exception) {}
    }
}

enum class VideoAspectMode(val label: String, val scale: ContentScale) {
    FIT("Fit Screen", ContentScale.Fit),
    FILL("Fill / Crop", ContentScale.Crop),
    STRETCH("Stretch", ContentScale.FillBounds)
}

@Composable
fun PlayerScreen(
    videoUrl: String,
    headers: Map<String, String>?,
    onBack: () -> Unit,
    title: String? = null,
    itemId: String? = null,
    poster: String? = null,
    isTvShow: Boolean = false,
    tvShowId: String? = null,
    seasonNumber: Int? = null,
    episodeNumber: Int? = null,
    episodeTitle: String? = null,
    startPositionMs: Long? = 0L,
    providerId: String? = null,
    providerLanguage: String? = null,
    onNextEpisode: (() -> Unit)? = null,
    onPreviousEpisode: (() -> Unit)? = null,
    onToggleFullscreen: (() -> Unit)? = null
) {
    var vlcFound by remember { mutableStateOf<Boolean?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentTime by remember { mutableStateOf(0L) }
    var totalTime by remember { mutableStateOf(0L) }
    var volume by remember { mutableStateOf(100) }
    var isMuted by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(AppSettings.data.defaultSpeed) }
    var aspectMode by remember { mutableStateOf(VideoAspectMode.FIT) }
    var hasResumedPosition by remember { mutableStateOf(false) }
    var currentFrameBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Side Settings Drawer
    var showSettingsDrawer by remember { mutableStateOf(false) }
    var activeDrawerTab by remember { mutableStateOf(0) } // 0: Quality, 1: Audio, 2: Subs, 3: Speed, 4: Aspect

    // Quality selection
    var selectedQualityLabel by remember { mutableStateOf("Auto (Best Quality)") }

    // Track selections
    var audioTracks by remember { mutableStateOf<List<TrackDescription>>(emptyList()) }
    var currentAudioTrack by remember { mutableStateOf<Int>(-1) }
    var subtitleTracks by remember { mutableStateOf<List<TrackDescription>>(emptyList()) }
    var currentSubtitleTrack by remember { mutableStateOf<Int>(-1) }
    var hasAutoSelectedAudio by remember { mutableStateOf(false) }

    var isInstalling by remember { mutableStateOf(false) }
    var installResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
    var mediaPlayerRef by remember { mutableStateOf<EmbeddedMediaPlayer?>(null) }
    var factoryRef by remember { mutableStateOf<MediaPlayerFactory?>(null) }

    // Clean display title (avoid duplicate repeats)
    val displayTitle = remember(title, isTvShow, seasonNumber, episodeNumber, episodeTitle) {
        if (title != null && !title.contains(" - S") && isTvShow && seasonNumber != null && episodeNumber != null) {
            "$title - S${seasonNumber}:E${episodeNumber}${if (!episodeTitle.isNullOrBlank()) " $episodeTitle" else ""}"
        } else {
            title ?: "Streamflix Player"
        }
    }

    // Check VLC availability
    LaunchedEffect(Unit) {
        vlcFound = withContext(Dispatchers.IO) {
            try {
                val vlcPaths = listOf(
                    "C:\\Program Files\\VideoLAN\\VLC",
                    "C:\\Program Files (x86)\\VideoLAN\\VLC",
                    "/usr/lib/x86_64-linux-gnu",
                    "/usr/lib",
                    "/usr/lib64",
                    "/usr/local/lib",
                    "/usr/lib/aarch64-linux-gnu",
                    "/snap/vlc/current/usr/lib",
                    "/Applications/VLC.app/Contents/MacOS/lib"
                )
                var foundPath: String? = null
                for (path in vlcPaths) {
                    val dir = File(path)
                    if (dir.exists()) {
                        val hasLibVlc = dir.listFiles()?.any { 
                            it.name == "libvlc.dll" || 
                            it.name.startsWith("libvlc.so") || 
                            it.name == "libvlc.dylib" 
                        } == true
                        if (hasLibVlc) {
                            foundPath = path
                            System.setProperty("jna.library.path", path)
                            com.sun.jna.NativeLibrary.addSearchPath("vlc", path)
                            com.sun.jna.NativeLibrary.addSearchPath("vlccore", path)
                            
                            val pluginsDir = listOf(
                                File(dir, "plugins"),
                                File(dir, "vlc/plugins"),
                                File("/usr/lib/x86_64-linux-gnu/vlc/plugins"),
                                File("/usr/lib/vlc/plugins"),
                                File("/usr/lib/aarch64-linux-gnu/vlc/plugins")
                            ).firstOrNull { it.exists() }
                            if (pluginsDir != null) {
                                System.setProperty("VLC_PLUGIN_PATH", pluginsDir.absolutePath)
                            }
                            break
                        }
                    }
                }
                val discovered = try { NativeDiscovery().discover() } catch (_: Throwable) { false }
                discovered || foundPath != null
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // Auto-hide controls after 4 seconds of inactivity when playing (unless settings drawer is open)
    LaunchedEffect(showControls, lastInteractionTime, isPlaying, isPaused, showSettingsDrawer) {
        if (showControls && isPlaying && !isPaused && !showSettingsDrawer) {
            val elapsed = System.currentTimeMillis() - lastInteractionTime
            val remaining = 4000L - elapsed
            if (remaining > 0) {
                delay(remaining)
            }
            if (System.currentTimeMillis() - lastInteractionTime >= 4000L && !showSettingsDrawer) {
                showControls = false
            }
        }
    }

    // Update time, progress & tracks periodically
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(500)
            mediaPlayerRef?.let { mp ->
                try {
                    currentTime = mp.status().time()
                    totalTime = mp.status().length()

                    // Resume start position if requested
                    if (!hasResumedPosition && startPositionMs != null && startPositionMs > 0 && totalTime > 0) {
                        hasResumedPosition = true
                        mp.controls().setTime(startPositionMs)
                    }

                    // Save watch history progress periodically
                    if (currentTime > 2000 && totalTime > 0) {
                        val saveId = itemId ?: title ?: "item"
                        WatchHistoryManager.saveProgress(
                            id = saveId,
                            title = title ?: "Video",
                            poster = poster,
                            isTvShow = isTvShow,
                            tvShowId = tvShowId,
                            seasonNumber = seasonNumber,
                            episodeNumber = episodeNumber,
                            episodeTitle = episodeTitle,
                            positionMs = currentTime,
                            durationMs = totalTime,
                            providerId = providerId
                        )
                    }

                    val aTracks = mp.audio().trackDescriptions()
                    if (aTracks != null && aTracks.isNotEmpty()) {
                        audioTracks = aTracks
                        currentAudioTrack = mp.audio().track()

                        if (!hasAutoSelectedAudio) {
                            hasAutoSelectedAudio = true
                            val targetLang = (providerLanguage ?: AppSettings.data.appLanguage ?: "en").lowercase().substringBefore("-")
                            val matchKeywords = when (targetLang) {
                                "de" -> listOf("german", "deutsch", "ger", "deu")
                                "ru" -> listOf("russian", "русский", "rus", "ru", "dub", "rezka", "lostfilm", "dvo", "mvo")
                                "es" -> listOf("spanish", "español", "espanol", "castellano", "latino", "esp", "spa")
                                "it" -> listOf("italian", "italiano", "ita")
                                "fr" -> listOf("french", "français", "francais", "fra", "fre")
                                "pl" -> listOf("polish", "polski", "pol")
                                "ja" -> listOf("japanese", "nihongo", "jpn", "jap")
                                else -> emptyList()
                            }
                            if (matchKeywords.isNotEmpty()) {
                                val matchedTrack = aTracks.firstOrNull { t ->
                                    val desc = (t.description() ?: "").lowercase()
                                    matchKeywords.any { kw -> desc.contains(kw) }
                                }
                                if (matchedTrack != null && matchedTrack.id() >= 0) {
                                    mp.audio().setTrack(matchedTrack.id())
                                    currentAudioTrack = matchedTrack.id()
                                }
                            }
                        }
                    }
                    
                    val sTracks = mp.subpictures().trackDescriptions()
                    if (sTracks != null && sTracks.isNotEmpty()) {
                        subtitleTracks = sTracks
                        currentSubtitleTrack = mp.subpictures().track()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Focus for keyboard shortcuts
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    fun notifyUserActivity() {
        lastInteractionTime = System.currentTimeMillis()
        if (!showControls) {
            showControls = true
        }
    }

    fun togglePlayPause() {
        notifyUserActivity()
        mediaPlayerRef?.let { mp ->
            if (mp.status().isPlaying) {
                mp.controls().pause()
                isPaused = true
            } else {
                mp.controls().play()
                isPaused = false
            }
        }
    }

    fun skipTime(deltaMs: Long) {
        notifyUserActivity()
        mediaPlayerRef?.controls()?.skipTime(deltaMs)
    }

    // Initialize VLC direct player when ready or when videoUrl changes
    LaunchedEffect(vlcFound, videoUrl) {
        if (vlcFound == true && videoUrl.isNotBlank()) {
            currentTime = 0L
            totalTime = 0L
            currentFrameBitmap = null
            isPlaying = false
            isPaused = false
            hasResumedPosition = false
            hasAutoSelectedAudio = false
            error = null

            withContext(Dispatchers.IO) {
                try {
                    val factory = factoryRef ?: MediaPlayerFactory("--no-video-title-show").also { factoryRef = it }
                    val mp = mediaPlayerRef ?: factory.mediaPlayers().newEmbeddedMediaPlayer().also { newMp ->
                        mediaPlayerRef = newMp
                        val renderer = DirectVideoRenderer { bitmap ->
                            currentFrameBitmap = bitmap
                        }
                        val videoSurface = factory.videoSurfaces().newVideoSurface(renderer, renderer, true)
                        newMp.videoSurface().set(videoSurface)

                        newMp.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                            override fun playing(mediaPlayer: MediaPlayer) {
                                isPlaying = true
                                isPaused = false
                                error = null
                            }
                            override fun paused(mediaPlayer: MediaPlayer) {
                                isPaused = true
                            }
                            override fun stopped(mediaPlayer: MediaPlayer) {
                                isPlaying = false
                            }
                            override fun finished(mediaPlayer: MediaPlayer) {
                                isPlaying = false
                                // Auto-play Next Episode if enabled
                                if (AppSettings.data.autoplayNextEpisode && onNextEpisode != null) {
                                    scope.launch { onNextEpisode() }
                                } else {
                                    scope.launch { onBack() }
                                }
                            }
                            override fun error(mediaPlayer: MediaPlayer) {
                                error = "Failed to play video"
                            }
                        })
                    }

                    val options = mutableListOf<String>()
                    headers?.get("Referer")?.let { options.add(":http-referrer=$it") }
                    headers?.get("User-Agent")?.let { options.add(":http-user-agent=$it") }
                    options.add(":no-video-title-show")

                    val resolvedMrl = if (videoUrl.startsWith("data:")) {
                        val base64Data = videoUrl.substringAfter("base64,")
                        val bytes = Base64.getDecoder().decode(base64Data)
                        val tempFile = File.createTempFile("streamflix_manifest_", ".m3u8")
                        tempFile.deleteOnExit()
                        tempFile.writeBytes(bytes)
                        tempFile.absolutePath
                    } else {
                        videoUrl
                    }

                    println("[Streamflix Direct Player] Playing MRL: $resolvedMrl with ${options.size} options")
                    mp.media().play(resolvedMrl, *options.toTypedArray())
                } catch (e: Exception) {
                    e.printStackTrace()
                    error = "Failed to initialize player: ${e.message}"
                }
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayerRef?.controls()?.stop()
                mediaPlayerRef?.release()
                factoryRef?.release()
            } catch (_: Exception) {}
        }
    }

    val canGoPrev = isTvShow && (onPreviousEpisode != null || (episodeNumber != null && episodeNumber > 1))
    val canGoNext = isTvShow && (onNextEpisode != null)

    // Filtered track lists without confusing "Deaktivieren" or "-1" tracks
    val cleanAudioTracks = remember(audioTracks) {
        audioTracks.filter { it.id() >= 0 && !it.description().isNullOrBlank() }
    }
    val cleanSubtitleTracks = remember(subtitleTracks) {
        subtitleTracks.filter { it.id() >= 0 && !it.description().isNullOrBlank() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Spacebar -> {
                            togglePlayPause()
                            true
                        }
                        Key.DirectionLeft -> {
                            skipTime(-10000)
                            true
                        }
                        Key.DirectionRight -> {
                            skipTime(10000)
                            true
                        }
                        Key.DirectionUp -> {
                            notifyUserActivity()
                            mediaPlayerRef?.let { mp ->
                                volume = (volume + 10).coerceIn(0, 150)
                                mp.audio().setVolume(volume)
                                isMuted = false
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            notifyUserActivity()
                            mediaPlayerRef?.let { mp ->
                                volume = (volume - 10).coerceIn(0, 150)
                                mp.audio().setVolume(volume)
                            }
                            true
                        }
                        Key.M -> {
                            notifyUserActivity()
                            isMuted = !isMuted
                            mediaPlayerRef?.audio()?.setMute(isMuted)
                            true
                        }
                        Key.F -> {
                            notifyUserActivity()
                            onToggleFullscreen?.invoke()
                            true
                        }
                        Key.N -> {
                            onNextEpisode?.invoke()
                            true
                        }
                        Key.P -> {
                            onPreviousEpisode?.invoke()
                            true
                        }
                        Key.Escape -> {
                            if (showSettingsDrawer) {
                                showSettingsDrawer = false
                            } else {
                                onBack()
                            }
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        notifyUserActivity()
                        if (showSettingsDrawer) {
                            showSettingsDrawer = false
                        } else {
                            showControls = !showControls
                        }
                    },
                    onDoubleTap = {
                        onToggleFullscreen?.invoke()
                    }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move) {
                            notifyUserActivity()
                        }
                    }
                }
            }
    ) {
        when {
            vlcFound == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            }
            vlcFound == false -> {
                VlcInstallScreen(
                    isInstalling = isInstalling,
                    installResult = installResult,
                    onInstall = {
                        isInstalling = true
                        scope.launch {
                            val success = withContext(Dispatchers.IO) { tryInstallVlc() }
                            isInstalling = false
                            if (success) {
                                installResult = "VLC installed! Restarting player..."
                                delay(1000)
                                vlcFound = withContext(Dispatchers.IO) {
                                    try { NativeDiscovery().discover() } catch (e: Exception) { false }
                                }
                            } else {
                                installResult = "Auto-install failed. Please install VLC manually from https://www.videolan.org"
                            }
                        }
                    },
                    onBack = onBack
                )
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("Playback Error", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = Color(0xFFCF6679), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))) {
                            Text("Back")
                        }
                    }
                }
            }
            else -> {
                // Video frame display
                if (currentFrameBitmap != null) {
                    Image(
                        bitmap = currentFrameBitmap!!,
                        contentDescription = "Video Frame",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = aspectMode.scale
                    )
                }

                // Centered Loading Spinner when buffering/starting
                if (currentFrameBitmap == null && isPlaying) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE50914), modifier = Modifier.size(56.dp))
                    }
                }

                // Controls Overlay with subtle transparent gradients
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Top Bar with gradient
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.75f), Color.Black.copy(alpha = 0.35f), Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                displayTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Spacer(Modifier.weight(1f))

                            // Settings Drawer Toggle (Gear)
                            IconButton(onClick = {
                                notifyUserActivity()
                                showSettingsDrawer = !showSettingsDrawer
                            }) {
                                Icon(Icons.Default.Settings, "Player Settings", tint = Color.White)
                            }

                            // Fullscreen toggle
                            if (onToggleFullscreen != null) {
                                IconButton(onClick = {
                                    notifyUserActivity()
                                    onToggleFullscreen()
                                }) {
                                    Icon(Icons.Default.Fullscreen, "Fullscreen", tint = Color.White)
                                }
                            }
                        }

                        // Center Controls: Prev Ep | -10s | Play/Pause | +10s | Next Ep
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Left Side: Previous Episode & Rewind 10s
                                Row(
                                    modifier = Modifier.width(140.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (canGoPrev) {
                                        IconButton(
                                            onClick = {
                                                notifyUserActivity()
                                                onPreviousEpisode?.invoke()
                                            },
                                            modifier = Modifier.size(46.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.SkipPrevious, "Previous Episode", tint = Color.White, modifier = Modifier.size(26.dp))
                                        }
                                        Spacer(Modifier.width(14.dp))
                                    }

                                    IconButton(
                                        onClick = { skipTime(-10000) },
                                        modifier = Modifier.size(52.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Replay10, "Rewind 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                                    }
                                }

                                Spacer(Modifier.width(20.dp))

                                // EXACT CENTER: Play / Pause Button
                                IconButton(
                                    onClick = { togglePlayPause() },
                                    modifier = Modifier.size(72.dp).background(Color(0xFFE50914), CircleShape)
                                ) {
                                    Icon(
                                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Spacer(Modifier.width(20.dp))

                                // Right Side: Forward 10s & Next Episode
                                Row(
                                    modifier = Modifier.width(140.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { skipTime(10000) },
                                        modifier = Modifier.size(52.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Forward10, "Forward 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                                    }

                                    if (canGoNext) {
                                        Spacer(Modifier.width(14.dp))
                                        IconButton(
                                            onClick = {
                                                notifyUserActivity()
                                                onNextEpisode?.invoke()
                                            },
                                            modifier = Modifier.size(46.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.SkipNext, "Next Episode", tint = Color.White, modifier = Modifier.size(26.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Controls Bar with gradient
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            // Seek Slider
                            if (totalTime > 0) {
                                Slider(
                                    value = (currentTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f),
                                    onValueChange = { fraction ->
                                        notifyUserActivity()
                                        mediaPlayerRef?.controls()?.setTime((fraction * totalTime).toLong())
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFE50914),
                                        activeTrackColor = Color(0xFFE50914),
                                        inactiveTrackColor = Color.Gray.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(22.dp)
                                )
                            }

                            // Time and secondary controls
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Time
                                Text(
                                    "${formatTime(currentTime)} / ${formatTime(totalTime)}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                // Bottom quick controls
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (canGoPrev) {
                                        IconButton(onClick = { onPreviousEpisode?.invoke() }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.SkipPrevious, "Previous Episode", tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    IconButton(onClick = { togglePlayPause() }, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            "Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    if (canGoNext) {
                                        IconButton(onClick = { onNextEpisode?.invoke() }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.SkipNext, "Next Episode", tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    // Volume toggle
                                    IconButton(
                                        onClick = {
                                            notifyUserActivity()
                                            isMuted = !isMuted
                                            mediaPlayerRef?.audio()?.setMute(isMuted)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (isMuted || volume == 0) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                            "Volume",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Side Settings Drawer / Menu (Streamflix Settings)
                AnimatedVisibility(
                    visible = showSettingsDrawer,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it }),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(350.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xF2161616),
                        tonalElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Player Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                IconButton(onClick = { showSettingsDrawer = false }) {
                                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Settings Tabs
                            ScrollableTabRow(
                                selectedTabIndex = activeDrawerTab,
                                containerColor = Color(0xFF222222),
                                contentColor = Color(0xFFE50914),
                                edgePadding = 4.dp
                            ) {
                                val tabs = listOf("Quality", "Audio", "Subs", "Speed", "Aspect")
                                tabs.forEachIndexed { index, tabTitle ->
                                    Tab(
                                        selected = activeDrawerTab == index,
                                        onClick = { activeDrawerTab = index },
                                        text = { Text(tabTitle, fontSize = 12.sp, color = if (activeDrawerTab == index) Color.White else Color.Gray) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Tab Contents
                            when (activeDrawerTab) {
                                0 -> { // Quality
                                    Text("Video Resolution / Quality", color = Color.Gray, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val qualityOptions = listOf(
                                            Pair("Auto (Best Quality)", "⚡ Automatic (Default)"),
                                            Pair("1080p Full HD", "✨ 1080p (Full HD)"),
                                            Pair("720p HD", "📺 720p (HD)"),
                                            Pair("480p SD", "📱 480p (SD)")
                                        )

                                        items(qualityOptions) { (key, label) ->
                                            val isSelected = selectedQualityLabel == key
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        selectedQualityLabel = key
                                                    }
                                                    .background(if (isSelected) Color(0xFFE50914).copy(alpha = 0.25f) else Color(0xFF222222))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = null,
                                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE50914))
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(label, color = Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }

                                        item {
                                            Spacer(Modifier.height(12.dp))
                                            Text("Active Provider / Server", color = Color.Gray, fontSize = 13.sp)
                                            Spacer(Modifier.height(6.dp))
                                            Surface(
                                                color = Color(0xFF1E1E1E),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text("Source: ${providerId ?: "Streamflix HD"}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    Text("Multi-source stream with automatic failover", color = Color.Gray, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                                1 -> { // Audio Tracks
                                    Text("Audio Track / Language", color = Color.Gray, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    if (cleanAudioTracks.isEmpty()) {
                                        Text("Default audio track active", color = Color.White, fontSize = 14.sp)
                                    } else {
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(cleanAudioTracks) { track ->
                                                val isSelected = track.id() == currentAudioTrack
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            mediaPlayerRef?.audio()?.setTrack(track.id())
                                                            currentAudioTrack = track.id()
                                                        }
                                                        .background(if (isSelected) Color(0xFFE50914).copy(alpha = 0.25f) else Color(0xFF222222))
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = null,
                                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE50914))
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(track.description() ?: "Track ${track.id()}", color = Color.White, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                                2 -> { // Subtitles
                                    Text("Subtitles", color = Color.Gray, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Off option
                                        item {
                                            val isOff = currentSubtitleTrack == -1
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        mediaPlayerRef?.subpictures()?.setTrack(-1)
                                                        currentSubtitleTrack = -1
                                                    }
                                                    .background(if (isOff) Color(0xFFE50914).copy(alpha = 0.25f) else Color(0xFF222222))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isOff,
                                                    onClick = null,
                                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE50914))
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text("Off", color = Color.White, fontSize = 14.sp)
                                            }
                                        }

                                        items(cleanSubtitleTracks) { track ->
                                            val isSelected = track.id() == currentSubtitleTrack
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        mediaPlayerRef?.subpictures()?.setTrack(track.id())
                                                        currentSubtitleTrack = track.id()
                                                    }
                                                    .background(if (isSelected) Color(0xFFE50914).copy(alpha = 0.25f) else Color(0xFF222222))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = null,
                                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE50914))
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(track.description() ?: "Track ${track.id()}", color = Color.White, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                                3 -> { // Playback Speed
                                    Text("Playback Speed", color = Color.Gray, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                        val isSelected = speed == currentSpeed
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    currentSpeed = speed
                                                    mediaPlayerRef?.controls()?.setRate(speed)
                                                }
                                                .background(if (isSelected) Color(0xFFE50914).copy(alpha = 0.25f) else Color(0xFF222222))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = null,
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE50914))
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("${speed}x", color = Color.White, fontSize = 14.sp)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                                4 -> { // Aspect Ratio
                                    Text("Screen Aspect Ratio", color = Color.Gray, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    VideoAspectMode.values().forEach { mode ->
                                        val isSelected = mode == aspectMode
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    aspectMode = mode
                                                }
                                                .background(if (isSelected) Color(0xFFE50914).copy(alpha = 0.25f) else Color(0xFF222222))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = null,
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE50914))
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(mode.label, color = Color.White, fontSize = 14.sp)
                                        }
                                        Spacer(Modifier.height(6.dp))
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

@Composable
private fun VlcInstallScreen(
    isInstalling: Boolean,
    installResult: String?,
    onInstall: () -> Unit,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).widthIn(max = 500.dp)
        ) {
            Text("🎬 VLC Player Required", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text(
                "Streamflix uses VLC to play videos inside the app. It's free and open-source.",
                color = Color.Gray, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            if (installResult != null) {
                Text(installResult, color = if (installResult.contains("installed")) Color(0xFF4CAF50) else Color(0xFFCF6679), textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
            }

            if (isInstalling) {
                CircularProgressIndicator(color = Color(0xFFE50914), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Installing VLC...", color = Color.White)
            } else {
                Button(
                    onClick = onInstall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                    modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
                ) {
                    Text("Install VLC Automatically", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                Spacer(Modifier.width(8.dp))
                Text("Back")
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}

private fun tryInstallVlc(): Boolean {
    val os = System.getProperty("os.name").lowercase()
    return try {
        val cmd = when {
            os.contains("win") -> listOf("winget", "install", "--id", "VideoLAN.VLC", "--accept-source-agreements", "--accept-package-agreements")
            os.contains("linux") -> listOf("sudo", "apt-get", "install", "-y", "vlc")
            os.contains("mac") -> listOf("brew", "install", "--cask", "vlc")
            else -> return false
        }
        val process = ProcessBuilder(cmd).inheritIO().start()
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}
