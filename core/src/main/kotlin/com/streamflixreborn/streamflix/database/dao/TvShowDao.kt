package com.streamflixreborn.streamflix.database.dao

import com.streamflixreborn.streamflix.models.TvShow

/**
 * Desktop stub for TvShowDao.
 * TODO: Implement with Exposed in Phase 2.
 */
interface TvShowDao {
    fun save(tvShow: TvShow) {}
    fun getAll(): List<TvShow> = emptyList()
}
