package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.utils.format
import com.streamflixreborn.streamflix.utils.toCalendar
import java.util.Calendar

class Episode(
    var id: String = "",
    var number: Int = 0,
    var title: String? = null,
    released: String? = null,
    var poster: String? = null,
    var overview: String? = null,
    var tvShow: TvShow? = null,
    var season: Season? = null,
) : WatchItem, Item {
    var released = released?.toCalendar()
    override var isWatched: Boolean = false
    override var watchedDate: Calendar? = null
    override var watchHistory: WatchItem.WatchHistory? = null

    fun isSame(episode: Episode): Boolean {
        if (isWatched != episode.isWatched) return false
        if (watchedDate != episode.watchedDate) return false
        if (watchHistory != episode.watchHistory) return false
        return true
    }

    fun merge(episode: Episode): Episode {
        this.isWatched = episode.isWatched
        this.watchedDate = episode.watchedDate
        this.watchHistory = episode.watchHistory
        return this
    }

    fun copy(
        id: String = this.id, number: Int = this.number, title: String? = this.title,
        overview: String? = this.overview, released: String? = this.released?.format("yyyy-MM-dd"),
        poster: String? = this.poster, tvShow: TvShow? = this.tvShow, season: Season? = this.season,
    ) = Episode(id, number, title, released, poster, overview, tvShow, season)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Episode
        return id == other.id && number == other.number
    }

    override fun hashCode(): Int = 31 * id.hashCode() + number
}
