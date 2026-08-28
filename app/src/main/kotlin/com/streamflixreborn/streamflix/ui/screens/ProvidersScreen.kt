package com.streamflixreborn.streamflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import com.streamflixreborn.streamflix.providers.TmdbProvider
import com.streamflixreborn.streamflix.ui.components.ProviderCard

fun getLanguageLabel(code: String): String {
    return when (code.lowercase()) {
        "en" -> "🇬🇧 English (en)"
        "de" -> "🇩🇪 German (de)"
        "ru" -> "🇷🇺 Russian (ru)"
        "es" -> "🇪🇸 Spanish (es)"
        "fr" -> "🇫🇷 French (fr)"
        "it" -> "🇮🇹 Italian (it)"
        "pl" -> "🇵🇱 Polish (pl)"
        "pt" -> "🇵🇹 Portuguese (pt)"
        "ja" -> "🇯🇵 Anime (ja)"
        "ar" -> "🇦🇷 Argentina (ar)"
        "mx" -> "🇲🇽 Mexico (mx)"
        "all" -> "🌐 All Languages"
        else -> "🌐 $code"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    currentProvider: Provider?,
    onProviderSelected: (Provider) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("All") }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    val allBaseProviders = remember { Provider.providers.keys.toList() }
    val languages = remember(allBaseProviders) {
        (allBaseProviders.map { it.language } + listOf("ru", "pt", "ja")).distinct().sorted()
    }
    
    val allProviders = remember(allBaseProviders, languages) {
        val tmdbProviders = listOf("en", "de", "es", "fr", "it", "pl", "pt", "ru").map { TmdbProvider(it) }
        allBaseProviders + tmdbProviders
    }

    val filteredProviders = remember(allProviders, selectedLanguage, searchQuery) {
        allProviders.filter { provider ->
            val langMatch = selectedLanguage == "All" || provider.language.equals(selectedLanguage, ignoreCase = true)
            val searchMatch = searchQuery.isBlank() || 
                provider.name.contains(searchQuery, ignoreCase = true) || 
                provider.language.contains(searchQuery, ignoreCase = true)
            langMatch && searchMatch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Select Content Provider", fontWeight = FontWeight.Bold)
                        Text("${filteredProviders.size} providers available", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(horizontal = 16.dp)
        ) {
            // Search Box and Dropdown Menu Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search 50+ providers by name or language...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE50914),
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Language Dropdown Selector Button
                Box {
                    Button(
                        onClick = { isLanguageDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(
                            if (selectedLanguage == "All") "🌐 All" else getLanguageLabel(selectedLanguage),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Language Menu", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { isLanguageDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF222222))
                    ) {
                        val allDropdownLangs = listOf("All") + languages
                        allDropdownLangs.forEach { langCode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (langCode == "All") "🌐 All Languages" else getLanguageLabel(langCode),
                                        color = if (selectedLanguage.equals(langCode, ignoreCase = true)) Color(0xFFE50914) else Color.White,
                                        fontWeight = if (selectedLanguage.equals(langCode, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedLanguage = langCode
                                    isLanguageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Language Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipLangs = listOf("All") + languages
                chipLangs.forEach { langCode ->
                    val isSelected = selectedLanguage.equals(langCode, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLanguage = langCode },
                        label = { Text(if (langCode == "All") "🌐 All" else getLanguageLabel(langCode)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE50914),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF222222),
                            labelColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Providers Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 240.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredProviders) { provider ->
                    val isSelected = provider.name == currentProvider?.name && provider.language == currentProvider?.language
                    val providerName = try { provider.name } catch (e: Exception) { "Unknown" }
                    val providerLogo = try { provider.logo } catch (e: Exception) { "" }
                    val providerLanguage = try { provider.language } catch (e: Exception) { "en" }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onProviderSelected(provider) }
                            .let {
                                if (isSelected) it.border(2.dp, Color(0xFFE50914), RoundedCornerShape(8.dp)) else it
                            },
                        color = if (isSelected) Color(0xFF2A1A1A) else Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        ProviderCard(
                            name = providerName,
                            logoUrl = providerLogo,
                            language = getLanguageLabel(providerLanguage),
                            isSelected = isSelected,
                            onClick = { onProviderSelected(provider) }
                        )
                    }
                }
            }
        }
    }
}
