package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamflixreborn.streamflix.providers.Provider
import com.streamflixreborn.streamflix.providers.TmdbProvider
import com.streamflixreborn.streamflix.ui.components.ProviderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    currentProvider: Provider?,
    onProviderSelected: (Provider) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("All Languages") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var favoriteProviders by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    val allBaseProviders = Provider.providers.keys.toList()
    val languages = allBaseProviders.map { it.language }.distinct().sorted()
    val languageOptions = listOf("All Languages") + languages
    
    val allProviders = remember(allBaseProviders) {
        val tmdbProviders = languages.map { TmdbProvider(it) }
        allBaseProviders + tmdbProviders
    }

    val filteredProviders = allProviders.filter {
        (selectedLanguage == "All Languages" || it.language == selectedLanguage) &&
        (it.name.contains(searchQuery, ignoreCase = true)) &&
        (!showFavoritesOnly || favoriteProviders.contains(it.name))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Provider") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF141414)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search providers...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE50914),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Box {
                    Button(
                        onClick = { isLanguageDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Text(selectedLanguage)
                    }
                    DropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { isLanguageDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF1A1A1A))
                    ) {
                        languageOptions.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang, color = Color.White) },
                                onClick = {
                                    selectedLanguage = lang
                                    isLanguageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = showFavoritesOnly,
                        onCheckedChange = { showFavoritesOnly = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE50914))
                    )
                    Text("Favorites Only", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProviders) { provider ->
                    val isSelected = provider == currentProvider
                    Box(
                        modifier = Modifier
                            .let {
                                if (isSelected) it.border(2.dp, Color(0xFFE50914)) else it
                            }
                            .clickable { onProviderSelected(provider) }
                    ) {
                        ProviderCard(
                            name = provider.name,
                            logoUrl = provider.logo,
                            language = provider.language,
                            isSelected = isSelected,
                            onClick = { onProviderSelected(provider) }
                        )
                    }
                }
            }
        }
    }
}
