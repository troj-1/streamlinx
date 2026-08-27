package com.streamflixreborn.streamflix.compat

/**
 * Minimal compatibility interface replacing AppAdapter.Item for JVM desktop.
 * All content items (Movie, TvShow, Episode, etc.) implement this.
 */
interface Item

/**
 * Compatibility enum replacing AppAdapter.Type
 */
enum class ItemType {
    MOVIE,
    TV_SHOW,
    EPISODE,
    SEASON,
    CATEGORY,
    GENRE,
    PEOPLE,
    PROVIDER,
    LOADING
}
