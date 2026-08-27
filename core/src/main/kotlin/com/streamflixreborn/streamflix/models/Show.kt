package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item

sealed interface Show : Item {
    var isFavorite: Boolean
}
