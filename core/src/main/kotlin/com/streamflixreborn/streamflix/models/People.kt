package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.utils.format
import com.streamflixreborn.streamflix.utils.toCalendar
import java.util.Calendar

class People(
    val id: String,
    val name: String,
    val image: String? = null,
    val biography: String? = null,
    val placeOfBirth: String? = null,
    birthday: String? = null,
    deathday: String? = null,
    val filmography: List<Show> = listOf(),
) : Item {
    val birthday: Calendar? = birthday?.toCalendar()
    val deathday: Calendar? = deathday?.toCalendar()

    fun copy(id: String = this.id, name: String = this.name, image: String? = this.image,
        biography: String? = this.biography, placeOfBirth: String? = this.placeOfBirth,
        birthday: String? = this.birthday?.format("yyyy-MM-dd"), deathday: String? = this.deathday?.format("yyyy-MM-dd"),
        filmography: List<Show> = this.filmography,
    ) = People(id, name, image, biography, placeOfBirth, birthday, deathday, filmography)

    override fun equals(other: Any?): Boolean { if (this === other) return true; if (javaClass != other?.javaClass) return false; other as People; return id == other.id && name == other.name }
    override fun hashCode(): Int = 31 * id.hashCode() + name.hashCode()
}
