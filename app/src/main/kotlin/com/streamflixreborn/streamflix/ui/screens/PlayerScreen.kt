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
    val scope = rememberCoroutineScope()

    LaunchedEffect(videoUrl) {
        try {
            withContext(Dispatchers.IO) {
                MpvPlayer.play(videoUrl, headers)
            }
            isPlaying = true
        } catch (e: Exception) {
            error = e.message
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (error != null) {
                Text("Playback Error", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(error!!, color = Color(0xFFCF6679), modifier = Modifier.padding(16.dp))
            } else {
                Text(
                    text = if (isPlaying) "\u25B6 Playing in mpv" else "\u23F3 Starting mpv...",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = videoUrl.take(80) + if (videoUrl.length > 80) "..." else "",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
                Spacer(Modifier.width(8.dp))
                Text("Back to Browse")
            }
        }
    }
}
