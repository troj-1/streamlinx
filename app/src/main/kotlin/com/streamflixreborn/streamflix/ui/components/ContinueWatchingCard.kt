package com.streamflixreborn.streamflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.utils.WatchHistoryEntry

@Composable
fun ContinueWatchingCard(
    entry: WatchHistoryEntry,
    onPlayClick: (WatchHistoryEntry) -> Unit,
    onDetailsClick: (WatchHistoryEntry) -> Unit,
    onRemoveClick: (WatchHistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (entry.durationMillis > 0) {
        (entry.lastPlaybackPositionMillis.toFloat() / entry.durationMillis.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.width(220.dp)
    ) {
        // Thumbnail & Play Button -> Triggers Direct Play
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(124.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1F1F1F))
                .clickable { onPlayClick(entry) }
        ) {
            AsyncImage(
                url = entry.poster,
                contentDescription = entry.title,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient shadow
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
            )

            // Red Play button icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFFE50914).copy(alpha = 0.95f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume Video",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3-dots Menu Button on top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF222222))
                ) {
                    DropdownMenuItem(
                        text = { Text("Resume", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White) },
                        onClick = {
                            showMenu = false
                            onPlayClick(entry)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Details", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.White) },
                        onClick = {
                            showMenu = false
                            onDetailsClick(entry)
                        }
                    )
                    HorizontalDivider(color = Color(0xFF333333))
                    DropdownMenuItem(
                        text = { Text("Remove from list", color = Color(0xFFFF5252)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                        onClick = {
                            showMenu = false
                            onRemoveClick(entry)
                        }
                    )
                }
            }

            // Progress bar at the bottom
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter),
                color = Color(0xFFE50914),
                trackColor = Color.DarkGray.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title and Subtitle Area -> Triggers Show/Movie Details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable { onDetailsClick(entry) }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (entry.isTvShow && entry.seasonNumber != null && entry.episodeNumber != null) {
                    Text(
                        text = "S${entry.seasonNumber}:E${entry.episodeNumber}${if (!entry.episodeTitle.isNullOrBlank()) " - ${entry.episodeTitle}" else ""}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    val mins = (entry.lastPlaybackPositionMillis / 60000)
                    val durMins = (entry.durationMillis / 60000)
                    Text(
                        text = "$mins mins of $durMins mins",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
