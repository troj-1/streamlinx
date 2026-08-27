package com.streamflixreborn.streamflix.utils

/**
 * Desktop stub for the AniWorld background worker.
 * The original uses Android WorkManager. On desktop, this is a no-op.
 */
object AniWorldUpdateTvShowWorker {
    fun enqueue(tvShowId: String) {
        // No-op on desktop — WorkManager is Android-only
    }
}
