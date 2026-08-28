package com.streamflixreborn.streamflix.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class FavoriteItem(
    val id: String,
    val title: String,
    val poster: String?,
    val banner: String? = null,
    val isTvShow: Boolean = false,
    val rating: Double? = null,
    val quality: String? = null,
    val overview: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

object FavoritesManager {
    private val gson = Gson()
    private val storageDir = File(System.getProperty("user.home"), ".streamflix").apply { mkdirs() }
    private val favoritesFile = File(storageDir, "favorites.json")
    private var favoritesList = mutableListOf<FavoriteItem>()

    init {
        loadFavorites()
    }

    @Synchronized
    fun getFavorites(): List<FavoriteItem> {
        return favoritesList.toList()
    }

    @Synchronized
    fun isFavorite(id: String): Boolean {
        return favoritesList.any { it.id == id }
    }

    @Synchronized
    fun addFavorite(item: FavoriteItem) {
        if (!isFavorite(item.id)) {
            favoritesList.add(0, item)
            saveFavorites()
        }
    }

    @Synchronized
    fun removeFavorite(id: String) {
        if (favoritesList.removeAll { it.id == id }) {
            saveFavorites()
        }
    }

    @Synchronized
    fun toggleFavorite(item: FavoriteItem): Boolean {
        return if (isFavorite(item.id)) {
            removeFavorite(item.id)
            false
        } else {
            addFavorite(item)
            true
        }
    }

    @Synchronized
    private fun loadFavorites() {
        try {
            if (favoritesFile.exists()) {
                val json = favoritesFile.readText()
                val type = object : TypeToken<List<FavoriteItem>>() {}.type
                val loaded: List<FavoriteItem>? = gson.fromJson(json, type)
                if (loaded != null) {
                    favoritesList = loaded.toMutableList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun saveFavorites() {
        try {
            val json = gson.toJson(favoritesList)
            favoritesFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
