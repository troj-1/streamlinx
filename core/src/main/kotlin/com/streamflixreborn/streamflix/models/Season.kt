package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item

class Season(
    var id: String = "",
    var number: Int = 0,
    var title: String? = null,
    var poster: String? = null,
    var tvShow: TvShow? = null,
    var episodes: List<Episode> = listOf(),
) : Item {
    fun copy(id: String = this.id, number: Int = this.number, title: String? = this.title,
        poster: String? = this.poster, tvShow: TvShow? = this.tvShow, episodes: List<Episode> = this.episodes,
    ) = Season(id, number, title, poster, tvShow, episodes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Season
        return id == other.id && number == other.number
    }

    override fun hashCode(): Int = 31 * id.hashCode() + number
}
