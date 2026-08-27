package com.streamflixreborn.streamflix.compat

/**
 * Android Log compatibility shim for JVM desktop.
 * Replaces android.util.Log with SLF4J-backed logging.
 */
object Log {
    private val logger = org.slf4j.LoggerFactory.getLogger("Streamflix")

    fun v(tag: String, msg: String) = logger.trace("[$tag] $msg")
    fun d(tag: String, msg: String) = logger.debug("[$tag] $msg")
    fun i(tag: String, msg: String) = logger.info("[$tag] $msg")
    fun w(tag: String, msg: String) = logger.warn("[$tag] $msg")
    fun e(tag: String, msg: String) = logger.error("[$tag] $msg")
    fun e(tag: String, msg: String, e: Throwable) = logger.error("[$tag] $msg", e)

    fun v(tag: String, msg: String, tr: Throwable) = logger.trace("[$tag] $msg", tr)
    fun d(tag: String, msg: String, tr: Throwable) = logger.debug("[$tag] $msg", tr)
    fun i(tag: String, msg: String, tr: Throwable) = logger.info("[$tag] $msg", tr)
    fun w(tag: String, msg: String, tr: Throwable) = logger.warn("[$tag] $msg", tr)
}
