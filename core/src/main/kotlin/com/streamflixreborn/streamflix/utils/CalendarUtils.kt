package com.streamflixreborn.streamflix.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun String.toCalendar(): Calendar? {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
    )
    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(this) ?: continue
            return Calendar.getInstance().apply { time = date }
        } catch (_: Exception) { }
    }
    return null
}

fun Calendar.format(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    return sdf.format(this.time)
}
