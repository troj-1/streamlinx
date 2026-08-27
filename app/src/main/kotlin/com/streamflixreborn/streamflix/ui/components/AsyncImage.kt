package com.streamflixreborn.streamflix.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.net.HttpURLConnection
import java.net.URL

object ImageCache {
    private val maxEntries = 200
    val cache = object : java.util.LinkedHashMap<String, ImageBitmap>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > maxEntries
        }
    }
}

@Composable
fun AsyncImage(
    url: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(ImageCache.cache[url]) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank() || url.endsWith(".svg", ignoreCase = true)) return@LaunchedEffect
        if (bitmap != null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connect()
                val bytes = connection.inputStream.readBytes()
                val skiaImage = Image.makeFromEncoded(bytes)
                val composeBitmap = skiaImage.toComposeImageBitmap()
                bitmap = composeBitmap
                synchronized(ImageCache) {
                    if (url != null) {
                        ImageCache.cache[url] = composeBitmap
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier = modifier.background(Color(0xFF2A2A2A)))
    }
}
