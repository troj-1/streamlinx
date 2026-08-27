package com.streamflixreborn.streamflix.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun String.toCalendar(): Calendar? {
    val patterns = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH),
    )
    for (sdf in patterns) {
        try {
            val date = sdf.parse(this) ?: continue
            return Calendar.getInstance().apply { time = date }
        } catch (_: Exception) { }
    }
    return null
}

fun Calendar.format(pattern: String): String? {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
        sdf.format(this.time)
    } catch (_: Exception) { null }
}

suspend fun <T> retry(retries: Int, predicate: suspend (attempt: Int) -> T): T {
    require(retries > 0) { "Expected positive amount of retries, but had $retries" }
    var throwable: Throwable? = null
    (1..retries).forEach { attempt ->
        try { return predicate(attempt) }
        catch (e: Throwable) { throwable = e }
    }
    throw throwable!!
}

fun <T> List<T>.safeSubList(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex > toIndex) return emptyList()
    return subList(
        max(min(fromIndex.coerceAtLeast(0), size), 0),
        max(min(toIndex.coerceAtMost(size), size), 0)
    )
}

fun <T> List<T>.findClosest(value: Float, selector: (T) -> Float): T? {
    return minByOrNull { abs(value - selector(it)) }
}

fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>

fun <T> CoroutineScope.asyncOrNull(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T?> = async(context, start) {
    try { block() } catch (_: Exception) { null }
}

fun String.toUri(): java.net.URI = java.net.URI(this)
