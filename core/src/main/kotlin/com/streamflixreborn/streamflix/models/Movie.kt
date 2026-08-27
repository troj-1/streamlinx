package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.utils.format
import com.streamflixreborn.streamflix.utils.toCalendar
import java.util.Calendar

class Movie(
    var id: String = "",
    var title: String = "",
    var overview: String? = null,
    released: String? = null,
    var runtime: Int? = null,
    var trailer: String? = null,
    var quality: String? = null,
    var rating: Double? = null,
    var poster: String? = null,
    var banner: String? = null,
    var imdbId: String? = null,
    var providerName: String? = null,
    val genres: List<Genre> = listOf(),
    val directors: List<People> = listOf(),
    val cast: List<People> = listOf(),
    val recommendations: List<Show> = listOf(),
    override var isFavorite: Boolean = false,
) : Show, WatchItem, Item {

    var released = released?.toCalendar()
    var favoritedAtMillis: Long? = null
    var lastPlayedAtMillis: Long? = null

    override var isWatched: Boolean = false
    override var watchedDate: Calendar? = null
    override var watchHistory: WatchItem.WatchHistory? = null

    fun isSame(movie: Movie): Boolean {
        if (isFavorite != movie.isFavorite) return false
        if (favoritedAtMillis != movie.favoritedAtMillis) return false
        if (isWatched != movie.isWatched) return false
        if (watchedDate != movie.watchedDate) return false
        if (watchHistory != movie.watchHistory) return false
        if (lastPlayedAtMillis != movie.lastPlayedAtMillis) return false
        return true
    }

    fun merge(movie: Movie): Movie {
        this.isFavorite = movie.isFavorite
        this.favoritedAtMillis = movie.favoritedAtMillis
        this.isWatched = movie.isWatched
        this.watchedDate = movie.watchedDate
        this.watchHistory = movie.watchHistory
        this.lastPlayedAtMillis = movie.lastPlayedAtMillis
        return this
    }

    fun copy(
        id: String = this.id,
        title: String = this.title,
        overview: String? = this.overview,
        released: String? = this.released?.format("yyyy-MM-dd"),
        runtime: Int? = this.runtime,
        trailer: String? = this.trailer,
        quality: String? = this.quality,
        rating: Double? = this.rating,
        poster: String? = this.poster,
        banner: String? = this.banner,
        imdbId: String? = this.imdbId,
        genres: List<Genre> = this.genres,
        directors: List<People> = this.directors,
        cast: List<People> = this.cast,
        recommendations: List<Show> = this.recommendations,
        isFavorite: Boolean = this.isFavorite,
    ) = Movie(
        id, title, overview, released, runtime, trailer, quality, rating,
        poster, banner, imdbId, providerName, genres, directors, cast,
        recommendations, isFavorite,
    ).apply {
        lastPlayedAtMillis = this@Movie.lastPlayedAtMillis
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Movie
        return id == other.id && title == other.title && overview == other.overview &&
            runtime == other.runtime && rating == other.rating && poster == other.poster &&
            banner == other.banner && isFavorite == other.isFavorite
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (poster?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        return result
    }
}
