package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item

class Category(
    var name: String,
    val list: List<Item>,
) : Item {
    var selectedIndex: Int = 0
    var itemSpacing: Int = 0

    fun copy(name: String = this.name, list: List<Item> = this.list) = Category(name, list)
    override fun equals(other: Any?): Boolean { if (this === other) return true; if (javaClass != other?.javaClass) return false; other as Category; return name == other.name && list == other.list }
    override fun hashCode(): Int = 31 * name.hashCode() + list.hashCode()

    companion object {
        const val FEATURED = ""
        const val CONTINUE_WATCHING = "Continue Watching"
        const val RECENTLY_WATCHED = "Recently Watched"
        const val FAVORITE_MOVIES = "Favorite movies"
        const val FAVORITE_TV_SHOWS = "Favorite TV shows"
    }
}
