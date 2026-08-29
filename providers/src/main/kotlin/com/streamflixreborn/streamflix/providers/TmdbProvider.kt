package com.streamflixreborn.streamflix.providers

import com.streamflixreborn.streamflix.compat.Item
import com.streamflixreborn.streamflix.extractors.Extractor
import com.streamflixreborn.streamflix.extractors.AfterDarkExtractor
import com.streamflixreborn.streamflix.extractors.MoflixExtractor
import com.streamflixreborn.streamflix.extractors.MoviesapiExtractor
import com.streamflixreborn.streamflix.extractors.TwoEmbedExtractor
import com.streamflixreborn.streamflix.extractors.VidsrcNetExtractor
import com.streamflixreborn.streamflix.extractors.VidsrcToExtractor
import com.streamflixreborn.streamflix.extractors.VidflixExtractor
import com.streamflixreborn.streamflix.extractors.VidzeeExtractor
import com.streamflixreborn.streamflix.extractors.VixSrcExtractor
import com.streamflixreborn.streamflix.extractors.EinschaltenExtractor
import com.streamflixreborn.streamflix.extractors.FrembedExtractor
import com.streamflixreborn.streamflix.extractors.VidrockExtractor
import com.streamflixreborn.streamflix.extractors.VideasyExtractor
import com.streamflixreborn.streamflix.extractors.PrimeSrcExtractor
import com.streamflixreborn.streamflix.extractors.RussianStreamExtractor
import com.streamflixreborn.streamflix.models.Category
import com.streamflixreborn.streamflix.models.Episode
import com.streamflixreborn.streamflix.models.Genre
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.People
import com.streamflixreborn.streamflix.models.Season
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.utils.TMDb3
import com.streamflixreborn.streamflix.utils.TMDb3.original
import com.streamflixreborn.streamflix.utils.TMDb3.w500
import com.streamflixreborn.streamflix.utils.UserPreferences
import com.streamflixreborn.streamflix.utils.safeSubList
import java.util.Base64
import com.streamflixreborn.streamflix.compat.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class TmdbProvider(override val language: String) : Provider {
    override val baseUrl: String
        get() = ""

    override val name = "TMDb ($language)"
    override val logo =
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Tmdb.new.logo.svg/1280px-Tmdb.new.logo.svg.png"

    override suspend fun getHome(): List<Category> = coroutineScope {
        val categories = mutableListOf<Category>()
        val watchRegion = if (language == "en") "US" else language.uppercase()

        val mapMulti: (TMDb3.MultiItem) -> Item? = { multi ->
            when (multi) {
                is TMDb3.Movie -> Movie(
                    id = multi.id.toString(),
                    title = multi.title,
                    overview = multi.overview,
                    released = multi.releaseDate,
                    rating = multi.voteAverage.toDouble(),
                    poster = multi.posterPath?.w500,
                    banner = multi.backdropPath?.original,
                )

                is TMDb3.Tv -> TvShow(
                    id = multi.id.toString(),
                    title = multi.name,
                    overview = multi.overview,
                    released = multi.firstAirDate,
                    rating = multi.voteAverage.toDouble(),
                    poster = multi.posterPath?.w500,
                    banner = multi.backdropPath?.original,
                )

                else -> null
            }
        }

        val trendingDeferred = async {
            awaitAll(
                async { TMDb3.Trending.all(TMDb3.Params.TimeWindow.DAY, page = 1, language = language) },
                async { TMDb3.Trending.all(TMDb3.Params.TimeWindow.DAY, page = 2, language = language) },
                async { TMDb3.Trending.all(TMDb3.Params.TimeWindow.DAY, page = 3, language = language) },
            ).flatMap { it.results }
        }

        val popularMoviesDeferred = async {
            awaitAll(
                async { TMDb3.MovieLists.popular(page = 1, language = language) },
                async { TMDb3.MovieLists.popular(page = 2, language = language) },
                async { TMDb3.MovieLists.popular(page = 3, language = language) },
            ).flatMap { it.results }
        }

        val popularTvShowsDeferred = async {
            awaitAll(
                async { TMDb3.TvSeriesLists.popular(page = 1, language = language) },
                async { TMDb3.TvSeriesLists.popular(page = 2, language = language) },
                async { TMDb3.TvSeriesLists.popular(page = 3, language = language) },
            ).flatMap { it.results }
        }

        val popularAnimeDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.movie(
                        language = language,
                        withKeywords = TMDb3.Params.WithBuilder(TMDb3.Keyword.KeywordId.ANIME)
                            .or(TMDb3.Keyword.KeywordId.BASED_ON_ANIME),
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withKeywords = TMDb3.Params.WithBuilder(TMDb3.Keyword.KeywordId.ANIME)
                            .or(TMDb3.Keyword.KeywordId.BASED_ON_ANIME),
                    )
                },
            ).flatMap { it.results }
        }

        val netflixDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.movie(
                        language = language,
                        watchRegion = watchRegion,
                        withWatchProviders = TMDb3.Params.WithBuilder(TMDb3.Provider.WatchProviderId.NETFLIX),
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.NETFLIX),
                    )
                },
            ).flatMap { it.results }
        }

        val amazonDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.movie(
                        language = language,
                        watchRegion = watchRegion,
                        withWatchProviders = TMDb3.Params.WithBuilder(TMDb3.Provider.WatchProviderId.AMAZON_VIDEO),
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.AMAZON),
                    )
                },
            ).flatMap { it.results }
        }

        val disneyDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.movie(
                        language = language,
                        watchRegion = watchRegion,
                        withWatchProviders = TMDb3.Params.WithBuilder(TMDb3.Provider.WatchProviderId.DISNEY_PLUS),
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.DISNEY_PLUS),
                    )
                },
            ).flatMap { it.results }
        }

        val huluDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.movie(
                        language = language,
                        watchRegion = watchRegion,
                        withWatchProviders = TMDb3.Params.WithBuilder(TMDb3.Provider.WatchProviderId.HULU),
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.HULU),
                    )
                },
            ).flatMap { it.results }
        }

        val appleDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.movie(
                        language = language,
                        watchRegion = watchRegion,
                        withWatchProviders = TMDb3.Params.WithBuilder(TMDb3.Provider.WatchProviderId.APPLE_TV_PLUS),
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.APPLE_TV),
                    )
                },
            ).flatMap { it.results }
        }

        val hboDeferred = async {
            awaitAll(
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.HBO),
                        page = 1,
                    )
                },
                async {
                    TMDb3.Discover.tv(
                        language = language,
                        withNetworks = TMDb3.Params.WithBuilder(TMDb3.Network.NetworkId.HBO),
                        page = 2,
                    )
                },
            ).flatMap { it.results }
        }

        val trending = trendingDeferred.await()
        categories.add(
            Category(
                name = Category.FEATURED,
                list = trending.safeSubList(0, 5).mapNotNull(mapMulti)
            )
        )

        categories.add(
            Category(
                name = getTranslation("Trending"),
                list = trending.safeSubList(5, trending.size).mapNotNull(mapMulti)
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular Movies"),
                list = popularMoviesDeferred.await().mapNotNull(mapMulti)
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular TV Shows"),
                list = popularTvShowsDeferred.await().mapNotNull(mapMulti)
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular Anime"),
                list = popularAnimeDeferred.await()
                    .sortedByDescending {
                        when (it) {
                            is TMDb3.Movie -> it.popularity
                            is TMDb3.Person -> it.popularity
                            is TMDb3.Tv -> it.popularity
                        }
                    }
                    .mapNotNull(mapMulti),
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular on Netflix"),
                list = netflixDeferred.await()
                    .sortedByDescending {
                        when (it) {
                            is TMDb3.Movie -> it.popularity
                            is TMDb3.Person -> it.popularity
                            is TMDb3.Tv -> it.popularity
                        }
                    }
                    .mapNotNull(mapMulti),
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular on Amazon"),
                list = amazonDeferred.await()
                    .sortedByDescending {
                        when (it) {
                            is TMDb3.Movie -> it.popularity
                            is TMDb3.Person -> it.popularity
                            is TMDb3.Tv -> it.popularity
                        }
                    }
                    .mapNotNull(mapMulti),
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular on Disney+"),
                list = disneyDeferred.await()
                    .sortedByDescending {
                        when (it) {
                            is TMDb3.Movie -> it.popularity
                            is TMDb3.Person -> it.popularity
                            is TMDb3.Tv -> it.popularity
                        }
                    }
                    .mapNotNull(mapMulti),
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular on Hulu"),
                list = huluDeferred.await()
                    .sortedByDescending {
                        when (it) {
                            is TMDb3.Movie -> it.popularity
                            is TMDb3.Person -> it.popularity
                            is TMDb3.Tv -> it.popularity
                        }
                    }
                    .mapNotNull(mapMulti),
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular on Apple TV+"),
                list = appleDeferred.await()
                    .sortedByDescending {
                        when (it) {
                            is TMDb3.Movie -> it.popularity
                            is TMDb3.Person -> it.popularity
                            is TMDb3.Tv -> it.popularity
                        }
                    }
                    .mapNotNull(mapMulti),
            )
        )

        categories.add(
            Category(
                name = getTranslation("Popular on HBO"),
                list = hboDeferred.await().mapNotNull(mapMulti),
            )
        )

        categories
    }

    override suspend fun search(query: String, page: Int): List<Item> {
        if (query.isEmpty()) {
            val genres = listOf(
                TMDb3.Genres.movieList(language = language),
                TMDb3.Genres.tvList(language = language),
            ).flatMap { it.genres }
                .distinctBy { it.id }
                .sortedBy { it.name }
                .map {
                    Genre(
                        id = it.id.toString(),
                        name = it.name,
                    )
                }

            return genres
        }

        val results = TMDb3.Search.multi(query, page = page, language = language).results.mapNotNull { multi ->
            when (multi) {
                is TMDb3.Movie -> Movie(
                    id = multi.id.toString(),
                    title = multi.title,
                    overview = multi.overview,
                    released = multi.releaseDate,
                    rating = multi.voteAverage.toDouble(),
                    poster = multi.posterPath?.w500,
                    banner = multi.backdropPath?.original,
                )

                is TMDb3.Tv -> TvShow(
                    id = multi.id.toString(),
                    title = multi.name,
                    overview = multi.overview,
                    released = multi.firstAirDate,
                    rating = multi.voteAverage.toDouble(),
                    poster = multi.posterPath?.w500,
                    banner = multi.backdropPath?.original,
                )

                else -> null
            }
        }

        return results
    }

    override suspend fun getMovies(page: Int): List<Movie> {
        val movies = TMDb3.MovieLists.popular(page = page, language = language).results.map { movie ->
            Movie(
                id = movie.id.toString(),
                title = movie.title,
                overview = movie.overview,
                released = movie.releaseDate,
                rating = movie.voteAverage.toDouble(),
                poster = movie.posterPath?.w500,
                banner = movie.backdropPath?.original,
            )
        }

        return movies
    }

    override suspend fun getTvShows(page: Int): List<TvShow> {
        val tvShows = TMDb3.TvSeriesLists.popular(page = page, language = language).results.map { tv ->
            TvShow(
                id = tv.id.toString(),
                title = tv.name,
                overview = tv.overview,
                released = tv.firstAirDate,
                rating = tv.voteAverage.toDouble(),
                poster = tv.posterPath?.w500,
                banner = tv.backdropPath?.original,
            )
        }

        return tvShows
    }

    override suspend fun getMovie(id: String): Movie {
        val movie = TMDb3.Movies.details(
            movieId = id.toInt(),
            appendToResponse = listOf(
                TMDb3.Params.AppendToResponse.Movie.CREDITS,
                TMDb3.Params.AppendToResponse.Movie.RECOMMENDATIONS,
                TMDb3.Params.AppendToResponse.Movie.VIDEOS,
                TMDb3.Params.AppendToResponse.Movie.EXTERNAL_IDS,
            ),
            language = language
        ).let { movie ->
            Movie(
                id = movie.id.toString(),
                title = movie.title,
                overview = movie.overview,
                released = movie.releaseDate,
                runtime = movie.runtime,
                trailer = movie.videos?.results
                    ?.sortedBy { it.publishedAt ?: "" }
                    ?.firstOrNull { it.site == TMDb3.Video.VideoSite.YOUTUBE }
                    ?.let { "https://www.youtube.com/watch?v=${it.key}" },
                rating = movie.voteAverage.toDouble(),
                poster = movie.posterPath?.original,
                banner = movie.backdropPath?.original,
                imdbId = movie.externalIds?.imdbId,

                genres = movie.genres.map { genre ->
                    Genre(
                        genre.id.toString(),
                        genre.name,
                    )
                },
                cast = movie.credits?.cast?.map { cast ->
                    People(
                        id = cast.id.toString(),
                        name = cast.name,
                        image = cast.profilePath?.w500,
                    )
                } ?: listOf(),
                recommendations = movie.recommendations?.results?.mapNotNull { multi ->
                    when (multi) {
                        is TMDb3.Movie -> Movie(
                            id = multi.id.toString(),
                            title = multi.title,
                            overview = multi.overview,
                            released = multi.releaseDate,
                            rating = multi.voteAverage.toDouble(),
                            poster = multi.posterPath?.w500,
                            banner = multi.backdropPath?.original,
                        )

                        is TMDb3.Tv -> TvShow(
                            id = multi.id.toString(),
                            title = multi.name,
                            overview = multi.overview,
                            released = multi.firstAirDate,
                            rating = multi.voteAverage.toDouble(),
                            poster = multi.posterPath?.w500,
                            banner = multi.backdropPath?.original,
                        )

                        else -> null
                    }
                } ?: listOf(),
            )
        }

        return movie
    }

    override suspend fun getTvShow(id: String): TvShow {
        val tvShow = TMDb3.TvSeries.details(
            seriesId = id.toInt(),
            appendToResponse = listOf(
                TMDb3.Params.AppendToResponse.Tv.CREDITS,
                TMDb3.Params.AppendToResponse.Tv.RECOMMENDATIONS,
                TMDb3.Params.AppendToResponse.Tv.VIDEOS,
                TMDb3.Params.AppendToResponse.Tv.EXTERNAL_IDS,
            ),
            language = language
        ).let { tv ->
            TvShow(
                id = tv.id.toString(),
                title = tv.name,
                overview = tv.overview,
                released = tv.firstAirDate,
                trailer = tv.videos?.results
                    ?.sortedBy { it.publishedAt ?: "" }
                    ?.firstOrNull { it.site == TMDb3.Video.VideoSite.YOUTUBE }
                    ?.let { "https://www.youtube.com/watch?v=${it.key}" },
                rating = tv.voteAverage.toDouble(),
                poster = tv.posterPath?.original,
                banner = tv.backdropPath?.original,
                imdbId = tv.externalIds?.imdbId,

                seasons = tv.seasons.map { season ->
                    Season(
                        id = "${tv.id}-${season.seasonNumber}",
                        number = season.seasonNumber,
                        title = season.name,
                        poster = season.posterPath?.w500,
                    )
                },
                genres = tv.genres.map { genre ->
                    Genre(
                        genre.id.toString(),
                        genre.name,
                    )
                },
                cast = tv.credits?.cast?.map { cast ->
                    People(
                        id = cast.id.toString(),
                        name = cast.name,
                        image = cast.profilePath?.w500,
                    )
                } ?: listOf(),
                recommendations = tv.recommendations?.results?.mapNotNull { multi ->
                    when (multi) {
                        is TMDb3.Movie -> Movie(
                            id = multi.id.toString(),
                            title = multi.title,
                            overview = multi.overview,
                            released = multi.releaseDate,
                            rating = multi.voteAverage.toDouble(),
                            poster = multi.posterPath?.w500,
                            banner = multi.backdropPath?.original,
                        )

                        is TMDb3.Tv -> TvShow(
                            id = multi.id.toString(),
                            title = multi.name,
                            overview = multi.overview,
                            released = multi.firstAirDate,
                            rating = multi.voteAverage.toDouble(),
                            poster = multi.posterPath?.w500,
                            banner = multi.backdropPath?.original,
                        )

                        else -> null
                    }
                } ?: listOf(),
            )
        }

        return tvShow
    }

    override suspend fun getEpisodesBySeason(seasonId: String): List<Episode> {
        val (tvShowId, seasonNumber) = seasonId.split("-")

        val episodes = TMDb3.TvSeasons.details(
            seriesId = tvShowId.toInt(),
            seasonNumber = seasonNumber.toInt(),
            language = language
        ).episodes?.map {
            Episode(
                id = it.id.toString(),
                number = it.episodeNumber,
                title = it.name ?: "",
                released = it.airDate,
                poster = it.stillPath?.w500,
            )
        } ?: listOf()

        return episodes
    }

    override suspend fun getGenre(id: String, page: Int): Genre {
        fun <T> List<T>.mix(other: List<T>): List<T> {
            return sequence {
                val first = iterator()
                val second = other.iterator()
                while (first.hasNext() && second.hasNext()) {
                    yield(first.next())
                    yield(second.next())
                }

                yieldAll(first)
                yieldAll(second)
            }.toList()
        }

        val genre = Genre(
            id = id,
            name = "",

            shows = TMDb3.Discover.movie(
                page = page,
                withGenres = TMDb3.Params.WithBuilder(id),
                language = language
            ).results.map { movie ->
                Movie(
                    id = movie.id.toString(),
                    title = movie.title,
                    overview = movie.overview,
                    released = movie.releaseDate,
                    rating = movie.voteAverage.toDouble(),
                    poster = movie.posterPath?.w500,
                    banner = movie.backdropPath?.original,
                )
            }.mix(TMDb3.Discover.tv(
                page = page,
                withGenres = TMDb3.Params.WithBuilder(id),
                language = language
            ).results.map { tv ->
                TvShow(
                    id = tv.id.toString(),
                    title = tv.name,
                    overview = tv.overview,
                    released = tv.firstAirDate,
                    rating = tv.voteAverage.toDouble(),
                    poster = tv.posterPath?.w500,
                    banner = tv.backdropPath?.original,
                )
            })
        )

        return genre
    }

    override suspend fun getPeople(id: String, page: Int): People {
        val people = TMDb3.People.details(
            personId = id.toInt(),
            appendToResponse = listOfNotNull(
                if (page > 1) null else TMDb3.Params.AppendToResponse.Person.COMBINED_CREDITS,
            ),
            language = language
        ).let { person ->
            People(
                id = person.id.toString(),
                name = person.name,
                image = person.profilePath?.w500,
                biography = person.biography,
                placeOfBirth = person.placeOfBirth,
                birthday = person.birthday,
                deathday = person.deathday,

                filmography = person.combinedCredits?.cast
                    ?.mapNotNull { multi ->
                        when (multi) {
                            is TMDb3.Movie -> Movie(
                                id = multi.id.toString(),
                                title = multi.title,
                                overview = multi.overview,
                                released = multi.releaseDate,
                                rating = multi.voteAverage.toDouble(),
                                poster = multi.posterPath?.w500,
                                banner = multi.backdropPath?.original,
                            )

                            is TMDb3.Tv -> TvShow(
                                id = multi.id.toString(),
                                title = multi.name,
                                overview = multi.overview,
                                released = multi.firstAirDate,
                                rating = multi.voteAverage.toDouble(),
                                poster = multi.posterPath?.w500,
                                banner = multi.backdropPath?.original,
                            )

                        else -> null
                    }
                }
                    ?.sortedBy {
                        when (it) {
                            is Movie -> it.released
                            is TvShow -> it.released
                        }
                    }
                    ?.reversed()
                    ?: listOf()
            )
        }

        return people
    }

    private suspend fun searchNativeProviders(
        providers: List<Provider>,
        videoType: Video.Type,
        filterKeywords: List<String> = emptyList()
    ): List<Video.Server> = coroutineScope {
        val targetTitle = when (videoType) {
            is Video.Type.Movie -> videoType.title
            is Video.Type.Episode -> videoType.tvShow.title
        }
        if (targetTitle.isBlank()) return@coroutineScope emptyList()

        fun isMatch(item: Item, target: String): Boolean {
            val isCorrectType = if (videoType is Video.Type.Movie) item is Movie else item is TvShow
            if (!isCorrectType) return false

            val itemTitle = if (item is Movie) item.title else (item as TvShow).title
            val nItem = itemTitle.lowercase().replace(Regex("[^a-z0-9а-яё]"), "")
            val nTarget = target.lowercase().replace(Regex("[^a-z0-9а-яё]"), "")
            
            if (nItem.isNotEmpty() && nTarget.isNotEmpty() && (nItem == nTarget || nItem.contains(nTarget) || nTarget.contains(nItem))) {
                return true
            }
            
            val cleanWords: (String) -> Set<String> = { s ->
                s.lowercase()
                    .replace(Regex("[^a-z0-9а-яё ]"), " ")
                    .split(Regex("\\s+"))
                    .filter { it.length > 2 }
                    .toSet()
            }
            val nItemWords = cleanWords(itemTitle)
            val nTargetWords = cleanWords(target)
            
            if (nItemWords.isEmpty() || nTargetWords.isEmpty()) return false
            if (nTargetWords.size == 1) return nItemWords.contains(nTargetWords.first())
            return nItemWords.containsAll(nTargetWords) || nTargetWords.containsAll(nItemWords)
        }

        val deferred = providers.map { provider ->
            async {
                try {
                    val searchResults = provider.search(targetTitle, 1)
                    val bestMatch = searchResults.firstOrNull { isMatch(it, targetTitle) }
                    val id = if (bestMatch is Movie) bestMatch.id else (bestMatch as? TvShow)?.id
                    
                    if (id != null) {
                        val allServers = provider.getServers(id, videoType)
                        if (filterKeywords.isEmpty()) {
                            allServers
                        } else {
                            val filtered = allServers.filter { s ->
                                val n = s.name.uppercase()
                                filterKeywords.any { kw -> n.contains(kw.uppercase()) }
                            }
                            if (filtered.isNotEmpty()) filtered else allServers
                        }
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        deferred.awaitAll().flatten()
    }

    override suspend fun getServers(id: String, videoType: Video.Type): List<Video.Server> {
        val servers = mutableListOf<Video.Server>()
        val lang = language.lowercase().substringBefore("-")

        Log.d("TmdbProvider", "getServers: lang=$language, simplifiedLang=$lang")

        val enrichedVideoType = when (videoType) {
            is Video.Type.Episode -> {
                if (videoType.tvShow.imdbId.isNullOrBlank() || videoType.tvShow.releaseDate.isNullOrBlank()) {
                    try {
                        val details = TMDb3.TvSeries.details(
                            seriesId = videoType.tvShow.id.toInt(),
                            appendToResponse = listOf(TMDb3.Params.AppendToResponse.Tv.EXTERNAL_IDS),
                            language = language
                        )
                        videoType.copy(
                            tvShow = videoType.tvShow.copy(
                                imdbId = details.externalIds?.imdbId ?: videoType.tvShow.imdbId,
                                releaseDate = details.firstAirDate ?: videoType.tvShow.releaseDate
                            )
                        )
                    } catch (e: Exception) {
                        videoType
                    }
                } else videoType
            }
            is Video.Type.Movie -> {
                if (videoType.imdbId.isNullOrBlank() || videoType.releaseDate.isBlank()) {
                    try {
                        val details = TMDb3.Movies.details(
                            movieId = videoType.id.toInt(),
                            language = language
                        )
                        videoType.copy(
                            imdbId = details.imdbId ?: videoType.imdbId,
                            releaseDate = details.releaseDate ?: videoType.releaseDate
                        )
                    } catch (e: Exception) {
                        videoType
                    }
                } else videoType
            }
        }

        suspend fun safeAdd(block: suspend () -> Unit) {
            try {
                block()
            } catch (t: Throwable) {
                Log.e("TmdbProvider", "Extractor error: ${t.message}")
            }
        }

        when (lang) {
            "ru" -> {
                // Russian servers: Native Russian Dubs first (Kodik / Collaps)
                safeAdd { servers.addAll(RussianStreamExtractor().servers(enrichedVideoType)) }
                safeAdd { servers.addAll(searchNativeProviders(listOf(MEGAKinoProvider, FilmyOnlineCcProvider), enrichedVideoType)) }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "ru")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "ru")) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            "de" -> {
                // German servers
                safeAdd { servers.addAll(searchNativeProviders(listOf(FilmPalastProvider, SerienStreamProvider, HDFilmeProvider, MEGAKinoProvider), enrichedVideoType)) }
                safeAdd { servers.addAll(MoflixExtractor().servers(enrichedVideoType)) }
                if (enrichedVideoType is Video.Type.Movie) {
                    safeAdd { servers.add(EinschaltenExtractor().server(enrichedVideoType)) }
                }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "de")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "de")) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            "it" -> {
                // Italian servers
                safeAdd { servers.addAll(searchNativeProviders(listOf(StreamingCommunityProvider("it"), Altadefinizione01Provider, CB01Provider, GuardaSerieProvider, AnimeWorldProvider, AnimeSaturnProvider), enrichedVideoType)) }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "it")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "it")) }
                safeAdd { servers.add(TwoEmbedExtractor().server(enrichedVideoType)) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            "fr" -> {
                // French servers
                safeAdd { servers.addAll(searchNativeProviders(listOf(FrenchStreamProvider, WiflixProvider, FrenchAnimeProvider, FrembedProvider), enrichedVideoType)) }
                safeAdd { servers.addAll(FrembedExtractor(UserPreferences.getProviderCache(FrembedProvider, UserPreferences.PROVIDER_URL) ?: FrembedProvider.baseUrl).servers(enrichedVideoType)) }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "fr")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "fr")) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            "pl" -> {
                // Polish servers
                safeAdd { servers.addAll(searchNativeProviders(listOf(FilmyOnlineCcProvider, ZaluknijProvider), enrichedVideoType)) }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "pl")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "pl")) }
                safeAdd { servers.add(TwoEmbedExtractor().server(enrichedVideoType)) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            "pt" -> {
                // Portuguese servers
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "pt")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "pt")) }
                safeAdd { servers.add(TwoEmbedExtractor().server(enrichedVideoType)) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            "es" -> {
                // Spanish servers
                safeAdd {
                    servers.addAll(searchNativeProviders(
                        listOf(CuevanaEuProvider, PelisplustoProvider, SoloLatinoProvider, CineCalidadProvider, PoseidonHD2Provider),
                        enrichedVideoType,
                        listOf("[LAT]", "[CAST]", "[CAS]", "[ES]", "(LAT)", "(ESP)", "LATINO", "CASTELLANO")
                    ))
                }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "es")) }
                safeAdd { servers.add(VixSrcExtractor().server(enrichedVideoType, "es")) }
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
            }
            else -> {
                // English (en) or other non-specific languages
                safeAdd { servers.addAll(PrimeSrcExtractor().servers(enrichedVideoType)) }
                safeAdd { servers.addAll(searchNativeProviders(listOf(SflixProvider, RidomoviesProvider), enrichedVideoType)) }
                safeAdd { servers.addAll(VideasyExtractor().servers(enrichedVideoType, "en")) }
                safeAdd { servers.add(VidsrcToExtractor().server(enrichedVideoType)) }
                safeAdd { servers.add(VidsrcNetExtractor().server(enrichedVideoType)) }
                safeAdd { servers.add(TwoEmbedExtractor().server(enrichedVideoType)) }
                safeAdd { servers.add(VidflixExtractor().server(enrichedVideoType)) }
                if (enrichedVideoType is Video.Type.Movie) {
                    safeAdd { servers.add(MoviesapiExtractor().server(enrichedVideoType)) }
                }
                safeAdd { servers.addAll(VidrockExtractor().servers(enrichedVideoType)) }
                safeAdd { servers.addAll(VidzeeExtractor().servers(enrichedVideoType)) }
            }
        }

        // Final sorting to prioritize target language servers and fast reliable extractors
        val finalServers = servers.sortedByDescending { server ->
            val n = server.name.uppercase()
            when (lang) {
                "ru" -> when {
                    n.contains("KODIK") || n.contains("COLLAPS") || n.contains("🇷🇺") || n.contains("DUB") || n.contains("RUS") -> 150
                    n.contains("VIXSRC") -> 120
                    n.contains("FILEMOON") -> 100
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                "de" -> when {
                    n.contains("MOFLIX") || n.contains("FILMPALAST") || n.contains("SERIENSTREAM") || n.contains("HDFILME") || n.contains("MEGAKINO") || n.contains("EINSCHALTEN") -> 150
                    n.contains("VIXSRC") -> 120
                    n.contains("FILEMOON") || n.contains("VOE") -> 100
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                "it" -> when {
                    n.contains("STREAMINGCOMMUNITY") || n.contains("ALTADEFINIZIONE") || n.contains("CB01") || n.contains("GUARDASERIE") || n.contains("ANIMWORLD") || n.contains("ANIMESATURN") -> 150
                    n.contains("VIXSRC") -> 140
                    n.contains("FILEMOON") -> 100
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                "es" -> when {
                    n.contains("FILEMOON") || n.contains("[CAS]") || n.contains("[LAT]") || n.contains("[ES]") || n.contains("SPAIN") || n.contains("[CAST]") ||
                    n.contains("LATINO") || n.contains("SPANISH") || n.contains("CASTELLANO") || n.contains("(LAT)") || n.contains("(ESP)") -> 150
                    n.contains("VIXSRC") -> 120
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                "fr" -> when {
                    n.contains("FRENCHSTREAM") || n.contains("WIFLIX") || n.contains("FRENCHANIME") || n.contains("FREMBED") -> 150
                    n.contains("VIXSRC") -> 120
                    n.contains("FILEMOON") -> 100
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                "pl" -> when {
                    n.contains("FILMYONLINE") || n.contains("ZALUKNIJ") -> 150
                    n.contains("VIXSRC") -> 120
                    n.contains("FILEMOON") -> 100
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                "pt" -> when {
                    n.contains("[PT]") || n.contains("[BR]") || n.contains("PORTUGUESE") || n.contains("DUBLADO") -> 150
                    n.contains("VIXSRC") -> 120
                    n.contains("FILEMOON") -> 100
                    n.contains("VIDEASY") -> 80
                    else -> 50
                }
                else -> when {
                    n.contains("FILEMOON") || n.contains("VOE") || n.contains("STREAMTAPE") || n.contains("PRIMESRC") -> 150
                    n.contains("SFLIX") || n.contains("RIDOMOVIES") -> 140
                    n.contains("VIDEASY") || n.contains("YORU") || n.contains("CYPHER") -> 130
                    n.contains("VIDSRC") || n.contains("2EMBED") || n.contains("VIDFLIX") -> 110
                    n.contains("VIDROCK") || n.contains("VIDZEE") -> 90
                    else -> 50
                }
            }
        }

        Log.i("StreamFlixES", "[SERVERS LIST] -> Found ${finalServers.size} servers: ${finalServers.joinToString { it.name }}")
        return finalServers.distinctBy { it.id }
    }

    override suspend fun getVideo(server: Video.Server): Video {
        val url = server.src.ifEmpty { server.id }
        Log.i("StreamFlixES", "[SERVER] -> Using: ${server.name} (URL: $url)")
        
        val video = when {
            server.video != null -> server.video!!
            else -> Extractor.extract(url, server)
        }

        // LOGICA SOTTOTITOLI FORZATI: Se siamo in spagnolo, attiviamo solo i forced di default
        if (language.startsWith("es")) {
            var forcedFound = false
            video.subtitles.forEach { sub ->
                val label = sub.label.lowercase()
                val isSpanish = label.contains("spanish") || label.contains("espaÃ±ol") || 
                                label.contains("espanol") || label.contains("castellano") || 
                                label.contains(" lat ")
                val isForced = label.contains("forced") || label.contains("forzati") || label.contains("forzato")

                if (isSpanish && isForced) {
                    sub.default = true
                    forcedFound = true
                    Log.i("StreamFlixES", "[SUBTITLE] -> TMDb (es): Selected FORCED subtitle: ${sub.label}")
                } else {
                    sub.default = false
                }
            }
            
            if (!forcedFound) {
                video.subtitles.forEach { it.default = false }
                Log.i("StreamFlixES", "[SUBTITLE] -> TMDb (es): No forced subs found, keeping them OFF")
            }
        }
        
        Log.i("StreamFlixES", "[VIDEO] -> Final source: ${video.source}")
        return video
    }

    private fun getTranslation(key: String): String {
        return when (language) {
            "it" -> when (key) {
                "Trending" -> "Di tendenza"
                "Popular Movies" -> "Film popolari"
                "Popular TV Shows" -> "Serie TV popolari"
                "Popular Anime" -> "Anime popolari"
                "Popular on Netflix" -> "Popolari su Netflix"
                "Popular on Amazon" -> "Popolari su Amazon"
                "Popular on Disney+" -> "Popolari su Disney+"
                "Popular on Hulu" -> "Popolari su Hulu"
                "Popular on Apple TV+" -> "Popolari su Apple TV+"
                "Popular on HBO" -> "Popolari su HBO"
                else -> key
            }
            "es" -> when (key) {
                "Trending" -> "Tendencias"
                "Popular Movies" -> "PelÃ­culas populares"
                "Popular TV Shows" -> "Series de TV populares"
                "Popular Anime" -> "Anime populares"
                "Popular on Netflix" -> "Popular en Netflix"
                "Popular on Amazon" -> "Popular en Amazon"
                "Popular on Disney+" -> "Popular en Disney+"
                "Popular on Hulu" -> "Popular en Hulu"
                "Popular on Apple TV+" -> "Popular en Apple TV+"
                "Popular on HBO" -> "Popular en HBO"
                else -> key
            }
            "de" -> when (key) {
                "Trending" -> "Trends"
                "Popular Movies" -> "Beliebte Filme"
                "Popular TV Shows" -> "Beliebte Serien"
                "Popular Anime" -> "Beliebte Anime"
                "Popular on Netflix" -> "Beliebt bei Netflix"
                "Popular on Amazon" -> "Beliebt bei Amazon"
                "Popular on Disney+" -> "Beliebt bei Disney+"
                "Popular on Hulu" -> "Beliebt bei Hulu"
                "Popular on Apple TV+" -> "Beliebt bei Apple TV+"
                "Popular on HBO" -> "Beliebt bei HBO"
                else -> key
            }
            "fr" -> when (key) {
                "Trending" -> "Tendances"
                "Popular Movies" -> "Films populaires"
                "Popular TV Shows" -> "SÃ©ries populaires"
                "Popular Anime" -> "Animes populaires"
                "Popular on Netflix" -> "Populaire sur Netflix"
                "Popular on Amazon" -> "Populaire sur Amazon"
                "Popular on Disney+" -> "Populaire sur Disney+"
                "Popular on Hulu" -> "Populaire sur Hulu"
                "Popular on Apple TV+" -> "Populaire sur Apple TV+"
                "Popular on HBO" -> "Populaire sur HBO"
                else -> key
            }
            else -> key
        }
    }
}
