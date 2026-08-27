package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.player.MpvPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlayerScreen(
    videoUrl: String,
    headers: Map<String, String>?,
    onBack: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isMpvMissing by remember { mutableStateOf(false) }
    var isInstalling by remember { mutableStateOf(false) }
    var installResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(videoUrl) {
        // Check if mpv is installed before trying to play
        val mpvFound = withContext(Dispatchers.IO) { MpvPlayer.isInstalled() }
        if (!mpvFound) {
            isMpvMissing = true
            return@LaunchedEffect
        }
        try {
            withContext(Dispatchers.IO) {
                MpvPlayer.play(videoUrl, headers)
            }
            isPlaying = true
        } catch (e: Exception) {
            if (e is MpvPlayer.MpvNotInstalledException) {
                isMpvMissing = true
            } else {
                error = e.message
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).widthIn(max = 600.dp)
        ) {
            when {
                isMpvMissing -> {
                    // mpv not installed — show install dialog
                    Text(
                        "\uD83C\uDFAC mpv Player Required",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Streamflix uses mpv for video playback. It's free, open-source, and lightweight.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    if (installResult != null) {
                        Text(
                            installResult!!,
                            color = if (installResult!!.contains("success", ignoreCase = true))
                                Color(0xFF4CAF50) else Color(0xFFCF6679),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    if (isInstalling) {
                        CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Installing mpv...", color = Color.White, fontSize = 14.sp)
                    } else {
                        // Auto-install button
                        Button(
                            onClick = {
                                isInstalling = true
                                scope.launch {
                                    val success = withContext(Dispatchers.IO) {
                                        MpvPlayer.tryAutoInstall()
                                    }
                                    isInstalling = false
                                    if (success) {
                                        installResult = "\u2705 mpv installed successfully! Click 'Try Playing Again' below."
                                        isMpvMissing = false
                                    } else {
                                        installResult = "\u274C Auto-install failed. Please install manually (see below)."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
                        ) {
                            Text("\uD83D\uDCE5 Install mpv Automatically", fontSize = 16.sp)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Manual instructions
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Manual Installation:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    MpvPlayer.getInstallInstructions(),
                                    color = Color(0xFFB0B0B0),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (installResult?.contains("success", ignoreCase = true) == true) {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                MpvPlayer.play(videoUrl, headers)
                                            }
                                            isPlaying = true
                                            isMpvMissing = false
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("\u25B6 Try Playing Again")
                            }
                        }
                    }
                }

                error != null -> {
                    Text("Playback Error", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = Color(0xFFCF6679), modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }

                else -> {
                    Text(
                        text = if (isPlaying) "\u25B6 Playing in mpv" else "\u23F3 Starting mpv...",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    if (!isPlaying) {
                        Spacer(Modifier.height(12.dp))
                        CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(36.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
                Spacer(Modifier.width(8.dp))
                Text("Back to Browse")
            }
        }
    }
}
