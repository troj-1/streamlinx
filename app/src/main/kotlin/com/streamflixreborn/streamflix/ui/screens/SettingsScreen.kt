package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage
import com.streamflixreborn.streamflix.utils.AppSettings
import com.streamflixreborn.streamflix.utils.Strings
import com.streamflixreborn.streamflix.utils.WatchHistoryManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentProvider: Provider?,
    onChangeProvider: () -> Unit
) {
    var selectedAppLang by remember { mutableStateOf(AppSettings.data.appLanguage) }
    var autoplay by remember { mutableStateOf(AppSettings.data.autoplayNextEpisode) }
    var subtitleSize by remember { mutableStateOf(AppSettings.data.subtitleSize) }
    var historyCleared by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("Settings", selectedAppLang), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Language Section
            item {
                Text(Strings.get("App Language", selectedAppLang), color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .padding(16.dp)
                ) {
                    val languages = listOf(
                        Pair("en", "🇬🇧 English"),
                        Pair("de", "🇩🇪 Deutsch"),
                        Pair("ru", "🇷🇺 Русский"),
                        Pair("es", "🇪🇸 Español"),
                        Pair("fr", "🇫🇷 Français"),
                        Pair("it", "🇮🇹 Italiano"),
                        Pair("pl", "🇵🇱 Polski"),
                        Pair("pt", "🇵🇹 Português")
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { (code, label) ->
                            val isSelected = selectedAppLang == code
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedAppLang = code
                                    AppSettings.update { appLanguage = code }
                                },
                                label = { Text(label, color = if (isSelected) Color.White else Color.Gray) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE50914),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2A2A2A)
                                )
                            )
                        }
                    }
                }
            }

            // Provider Section
            item {
                Text(Strings.get("Current Provider", selectedAppLang), color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        url = currentProvider?.logo,
                        modifier = Modifier.size(44.dp),
                        contentDescription = "Logo"
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(currentProvider?.name ?: "None", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Language: ${currentProvider?.language?.uppercase() ?: "Default"}", color = Color.Gray, fontSize = 13.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onChangeProvider, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))) {
                        Text(Strings.get("Change Provider", selectedAppLang))
                    }
                }
            }

            // Playback Section
            item {
                Text(Strings.get("Playback Settings", selectedAppLang), color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Autoplay Next Episode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Strings.get("Autoplay Next Episode", selectedAppLang), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Automatically transition to next episode", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = autoplay,
                            onCheckedChange = {
                                autoplay = it
                                AppSettings.update { autoplayNextEpisode = it }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFE50914)
                            )
                        )
                    }
                }
            }

            // Subtitles Section
            item {
                Text(Strings.get("Subs", selectedAppLang), color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .padding(16.dp)
                ) {
                    Text("Subtitle Text Size", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Small", "Medium", "Large").forEach { size ->
                            val isSelected = size == subtitleSize
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    subtitleSize = size
                                    AppSettings.update { this.subtitleSize = size }
                                },
                                label = { Text(size, color = if (isSelected) Color.White else Color.Gray) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE50914),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2A2A2A)
                                )
                            )
                        }
                    }
                }
            }

            // History Management
            item {
                Text("History & Storage", color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Strings.get("Clear Watch History", selectedAppLang), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Remove all saved progress and continue watching items", color = Color.Gray, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                WatchHistoryManager.getHistory().forEach { WatchHistoryManager.removeEntry(it.id) }
                                historyCleared = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                        ) {
                            Text(if (historyCleared) "Cleared!" else "Clear")
                        }
                    }
                }
            }

            // About
            item {
                Text("About", color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1F1F1F))
                        .padding(16.dp)
                ) {
                    Text("Streamflix Reborn Desktop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Version 1.0.0 (Linux & Windows)", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Ported with Jetpack Compose Multiplatform & LibVLC", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
