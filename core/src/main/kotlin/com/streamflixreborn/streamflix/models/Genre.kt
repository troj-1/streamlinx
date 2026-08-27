package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item

class Genre(
    val id: String,
    val name: String,
    val shows: List<Show> = listOf(),
) : Item {
    fun copy(id: String = this.id, name: String = this.name, shows: List<Show> = this.shows) = Genre(id, name, shows)
    override fun equals(other: Any?): Boolean { if (this === other) return true; if (javaClass != other?.javaClass) return false; other as Genre; return id == other.id && name == other.name }
    override fun hashCode(): Int = 31 * id.hashCode() + name.hashCode()
}
