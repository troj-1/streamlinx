package com.streamflixreborn.streamflix.models

import com.streamflixreborn.streamflix.compat.Item

/**
 * Provider model class for UI display.
 * Uses Any for the provider reference to avoid circular module dependency.
 */
open class Provider(
    val name: String,
    val logo: String,
    val language: String,
    val provider: Any,  // Reference to providers.Provider, resolved at runtime
    var isFavorite: Boolean = false,
) : Item
