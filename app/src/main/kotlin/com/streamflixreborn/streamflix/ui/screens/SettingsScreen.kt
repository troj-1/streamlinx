package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.ui.components.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentProvider: Provider?,
    onChangeProvider: () -> Unit
) {
    var tmdbKey by remember { mutableStateOf(System.getenv("TMDB_API_KEY") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            item {
                Text("Current Provider", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).padding(16.dp)) {
                    AsyncImage(
                        url = currentProvider?.logo,
                        modifier = Modifier.size(48.dp),
                        contentDescription = "Logo"
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(currentProvider?.name ?: "None", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(currentProvider?.language ?: "", color = Color.Gray)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onChangeProvider, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))) {
                        Text("Change")
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                Text("API Keys", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tmdbKey,
                    onValueChange = { tmdbKey = it },
                    label = { Text("TMDB API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE50914),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                Text("About", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Streamflix Reborn Desktop", color = Color.LightGray)
                Text("Version 1.0.0", color = Color.Gray)
            }
        }
    }
}
