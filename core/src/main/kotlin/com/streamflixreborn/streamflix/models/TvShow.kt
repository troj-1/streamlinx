package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.utils.format
import com.streamflixreborn.streamflix.utils.toCalendar

class TvShow(
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
    val seasons: List<Season> = listOf(),
    val genres: List<Genre> = listOf(),
    val directors: List<People> = listOf(),
    val cast: List<People> = listOf(),
    val recommendations: List<Show> = listOf(),
    override var isFavorite: Boolean = false,
) : Show, Item {

    var released = released?.toCalendar()
    var favoritedAtMillis: Long? = null
    var lastPlayedAtMillis: Long? = null
    var lastPlayedEpisodeId: String? = null
    var lastPlayedEpisode: Episode? = null
    var isWatching: Boolean = true

    val episodeToWatch: Episode?
        get() {
            val sortedSeasons = seasons.sortedWith(compareBy<Season> { it.number == 0 }.thenBy { it.number })
            val episodes = sortedSeasons.flatMap { season ->
                season.episodes.sortedBy { it.number }.onEach { episode ->
                    episode.season = season
                    episode.tvShow = this
                }
            }
            return episodes.filter { it.watchHistory != null }
                .sortedByDescending { it.watchHistory?.lastEngagementTimeUtcMillis }
                .firstOrNull()
                ?: episodes.indexOfLast { it.isWatched }
                    .takeIf { it != -1 && it + 1 < episodes.size }
                    ?.let { episodes.getOrNull(it + 1) }
                ?: sortedSeasons.firstOrNull { it.number != 0 }?.episodes?.sortedBy { it.number }?.firstOrNull()
                ?: episodes.firstOrNull()
        }

    fun isSame(tvShow: TvShow): Boolean {
        if (isFavorite != tvShow.isFavorite) return false
        if (favoritedAtMillis != tvShow.favoritedAtMillis) return false
        if (isWatching != tvShow.isWatching) return false
        if (lastPlayedAtMillis != tvShow.lastPlayedAtMillis) return false
        if (lastPlayedEpisodeId != tvShow.lastPlayedEpisodeId) return false
        return true
    }

    fun merge(tvShow: TvShow): TvShow {
        this.isFavorite = tvShow.isFavorite
        this.favoritedAtMillis = tvShow.favoritedAtMillis
        this.isWatching = tvShow.isWatching
        this.lastPlayedAtMillis = tvShow.lastPlayedAtMillis
        this.lastPlayedEpisodeId = tvShow.lastPlayedEpisodeId
        this.lastPlayedEpisode = tvShow.lastPlayedEpisode
        return this
    }

    fun copy(
        id: String = this.id, title: String = this.title, overview: String? = this.overview,
        released: String? = this.released?.format("yyyy-MM-dd"), runtime: Int? = this.runtime,
        trailer: String? = this.trailer, quality: String? = this.quality, rating: Double? = this.rating,
        poster: String? = this.poster, banner: String? = this.banner, imdbId: String? = this.imdbId,
        seasons: List<Season> = this.seasons, genres: List<Genre> = this.genres,
        directors: List<People> = this.directors, cast: List<People> = this.cast,
        recommendations: List<Show> = this.recommendations, isFavorite: Boolean = this.isFavorite
    ) = TvShow(id, title, overview, released, runtime, trailer, quality, rating, poster, banner,
        imdbId, providerName, seasons, genres, directors, cast, recommendations, isFavorite).apply {
        lastPlayedAtMillis = this@TvShow.lastPlayedAtMillis
        lastPlayedEpisodeId = this@TvShow.lastPlayedEpisodeId
        lastPlayedEpisode = this@TvShow.lastPlayedEpisode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TvShow
        return id == other.id && title == other.title
    }

    override fun hashCode(): Int = 31 * id.hashCode() + title.hashCode()
}
