package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item

open class Provider(
    val name: String,
    val logo: String,
    val language: String,
    val provider: com.streamflixreborn.streamflix.providers.Provider,
    var isFavorite: Boolean = false,
) : Item
