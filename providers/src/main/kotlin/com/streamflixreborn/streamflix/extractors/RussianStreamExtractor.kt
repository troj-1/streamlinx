package com.streamflixreborn.streamflix.extractors

import com.streamflixreborn.streamflix.compat.Log
import com.streamflixreborn.streamflix.models.Video
import com.streamflixreborn.streamflix.utils.UserPreferences
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class RussianStreamExtractor : Extractor() {
    override val name = "Kodik / Russian Dub"
    override val mainUrl = "https://kodikapi.com"
    override val aliasUrls = listOf(
        "https://kodik.info",
        "https://kodik.cc",
        "https://kodik.biz",
        "https://api.delivembed.cc",
        "https://delivembed.cc",
        "https://collaps.org",
        "https://embcoll.org"
    )

    private val client = com.streamflixreborn.streamflix.utils.NetworkClient.default

    private val kodikHosts = listOf(
        "https://kodikapi.com",
        "https://kodik-api.com",
        "https://kodik.biz",
        "https://kodik.cc"
    )

    private val kodikTokens = listOf(
        "04945952c4217be57223b564551187c3",
        "e67f079d342966e76121481e87515725",
        "836894a4b277d33d980de9de5290b632"
    )

    private fun getImdbId(isMovie: Boolean, tmdbId: String): String? {
        return try {
            val type = if (isMovie) "movie" else "tv"
            val key = UserPreferences.tmdbApiKey.ifBlank { "adc5047f27e588c9347087931a696cf4" }
            val req = Request.Builder()
                .url("https://api.themoviedb.org/3/$type/$tmdbId/external_ids?api_key=$key")
                .build()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body?.string().orEmpty())
            val id = json.optString("imdb_id")
            if (id.isNotBlank() && id != "null") id else null
        } catch (_: Exception) {
            null
        }
    }

    fun servers(videoType: Video.Type): List<Video.Server> {
        val servers = mutableListOf<Video.Server>()

        val isMovie = videoType is Video.Type.Movie
        val tmdbId = when (videoType) {
            is Video.Type.Movie -> videoType.id
            is Video.Type.Episode -> videoType.tvShow.id
        }

        val title = when (videoType) {
            is Video.Type.Movie -> videoType.title
            is Video.Type.Episode -> videoType.tvShow.title
        }

        val season = if (videoType is Video.Type.Episode) videoType.season.number else 1
        val episode = if (videoType is Video.Type.Episode) videoType.number else 1

        val imdbId = getImdbId(isMovie, tmdbId)

        // 1. Collaps Russian Server (IMDb & TMDb mapped)
        val collapsUrl = if (imdbId != null) {
            if (isMovie) {
                "https://api.delivembed.cc/embed/imdb/$imdbId"
            } else {
                "https://api.delivembed.cc/embed/imdb/$imdbId?season=$season&episode=$episode"
            }
        } else {
            if (isMovie) {
                "https://api.delivembed.cc/embed/movie/$tmdbId"
            } else {
                "https://api.delivembed.cc/embed/series/$tmdbId/$season/$episode"
            }
        }

        servers.add(Video.Server(
            id = "Collaps (RU)",
            name = "Collaps 🇷🇺 (Dubbed)",
            src = collapsUrl
        ))

        // 2. Kodik Russian Multi-Voice Server Search across host mirrors
        for (host in kodikHosts) {
            var foundOnHost = false
            for (token in kodikTokens) {
                try {
                    val queryUrl = when (videoType) {
                        is Video.Type.Movie -> {
                            "$host/search?token=$token&tmdb_id=$tmdbId&with_material_data=true&limit=15"
                        }
                        is Video.Type.Episode -> {
                            "$host/search?token=$token&tmdb_id=$tmdbId&season=$season&episode=$episode&with_material_data=true&limit=15"
                        }
                    }

                    val req = Request.Builder()
                        .url(queryUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .build()

                    val resp = client.newCall(req).execute()
                    val body = resp.body?.string().orEmpty()
                    if (body.startsWith("{")) {
                        val json = JSONObject(body)
                        val results = json.optJSONArray("results")
                        if (results != null && results.length() > 0) {
                            for (i in 0 until results.length()) {
                                val item = results.getJSONObject(i)
                                val link = item.optString("link")
                                val translation = item.optJSONObject("translation")
                                val transTitle = translation?.optString("title") ?: "Russian Dub"
                                if (link.isNotBlank()) {
                                    val fullLink = if (link.startsWith("//")) "https:$link" else link
                                    servers.add(Video.Server(
                                        id = "Kodik: $transTitle",
                                        name = "Kodik 🇷🇺 ($transTitle)",
                                        src = fullLink
                                    ))
                                }
                            }
                            if (servers.size > 1) {
                                foundOnHost = true
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RussianStreamExtractor", "Kodik search error on $host: ${e.message}")
                }
            }
            if (foundOnHost) break
        }

        // 3. Fallback Title Search on Kodik if TMDb ID search returned only Collaps
        if (servers.size <= 1 && title.isNotBlank()) {
            try {
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val searchUrl = "${kodikHosts[0]}/search?token=${kodikTokens[0]}&title=$encodedTitle&with_material_data=true&limit=10"
                val req = Request.Builder().url(searchUrl).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)").build()
                val resp = client.newCall(req).execute()
                val body = resp.body?.string().orEmpty()
                if (body.startsWith("{")) {
                    val results = JSONObject(body).optJSONArray("results")
                    if (results != null) {
                        for (i in 0 until results.length()) {
                            val item = results.getJSONObject(i)
                            val link = item.optString("link")
                            val translation = item.optJSONObject("translation")
                            val transTitle = translation?.optString("title") ?: "Russian Dub"
                            if (link.isNotBlank()) {
                                val fullLink = if (link.startsWith("//")) "https:$link" else link
                                servers.add(Video.Server(
                                    id = "Kodik: $transTitle",
                                    name = "Kodik 🇷🇺 ($transTitle)",
                                    src = fullLink
                                ))
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return servers
    }

    override suspend fun extract(link: String): Video {
        Log.i("RussianStreamExtractor", "Extracting stream from: $link")

        // 1. Check Collaps embed
        if (link.contains("delivembed") || link.contains("collaps") || link.contains("embcoll")) {
            return extractCollaps(link)
        }

        // 2. Check Kodik embed
        if (link.contains("kodik")) {
            return extractKodik(link)
        }

        throw Exception("Unknown Russian stream link: $link")
    }

    private fun extractCollaps(url: String): Video {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .header("Referer", "https://api.delivembed.cc/")
            .build()

        val resp = client.newCall(req).execute()
        var html = resp.body?.string().orEmpty()

        val iframeMatcher = Pattern.compile("""<iframe[^>]+src=['"]([^'"]+)['"]""").matcher(html)
        if (iframeMatcher.find()) {
            val iframeSrc = iframeMatcher.group(1)
            val fullIframeUrl = if (iframeSrc.startsWith("//")) "https:$iframeSrc" else if (iframeSrc.startsWith("http")) iframeSrc else "https://api.delivembed.cc/$iframeSrc"
            try {
                val iframeReq = Request.Builder().url(fullIframeUrl).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)").header("Referer", url).build()
                html = client.newCall(iframeReq).execute().body?.string().orEmpty()
            } catch (_: Exception) {}
        }

        val httpUrl = url.toHttpUrlOrNull()
        val targetSeason = httpUrl?.queryParameter("season")?.toIntOrNull() ?: 1
        val targetEpisode = httpUrl?.queryParameter("episode")?.toIntOrNull() ?: 1

        // Parse makePlayer({ ... }) JSON payload from Collaps player script
        val makePlayerMatcher = Pattern.compile("""makePlayer\s*\(\s*(\{.+?\})\s*\);""", Pattern.DOTALL).matcher(html)
        if (makePlayerMatcher.find()) {
            try {
                val jsonStr = makePlayerMatcher.group(1)
                val playerJson = JSONObject(jsonStr)
                val playlist = playerJson.optJSONObject("playlist")
                val seasons = playlist?.optJSONArray("seasons")
                if (seasons != null) {
                    for (s in 0 until seasons.length()) {
                        val sObj = seasons.getJSONObject(s)
                        val sNum = sObj.optInt("season", s + 1)
                        if (sNum == targetSeason || (seasons.length() == 1 && targetSeason == 1)) {
                            val eps = sObj.optJSONArray("episodes")
                            if (eps != null) {
                                for (e in 0 until eps.length()) {
                                    val epObj = eps.getJSONObject(e)
                                    val epNum = epObj.optString("episode").toIntOrNull() ?: (e + 1)
                                    if (epNum == targetEpisode) {
                                        val hls = epObj.optString("hls").ifBlank { epObj.optString("dash") }
                                        if (hls.isNotBlank()) {
                                            return Video(
                                                source = hls,
                                                headers = mapOf(
                                                    "Referer" to "https://api.delivembed.cc/",
                                                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Fallback for single movie in source object
                val sourceObj = playerJson.optJSONObject("source")
                val movieHls = sourceObj?.optString("hls")?.ifBlank { sourceObj.optString("dash") }
                if (!movieHls.isNullOrBlank()) {
                    return Video(
                        source = movieHls,
                        headers = mapOf(
                            "Referer" to "https://api.delivembed.cc/",
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("RussianStreamExtractor", "Failed to parse makePlayer JSON: ${e.message}")
            }
        }

        // Match hls playlist url from Collaps player scripts
        val m3u8Matcher = Pattern.compile("""(?:"hls"|hls|manifest|file):\s*['"](https?://[^'"]+?\.m3u8[^'"]*?)['"]""").matcher(html)
        if (m3u8Matcher.find()) {
            val manifestUrl = m3u8Matcher.group(1)
            return Video(
                source = manifestUrl,
                headers = mapOf("Referer" to url, "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            )
        }

        // Secondary regex matching any m3u8 URL in the page
        val fallbackMatcher = Pattern.compile("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").matcher(html)
        if (fallbackMatcher.find()) {
            return Video(
                source = fallbackMatcher.group(0),
                headers = mapOf("Referer" to url)
            )
        }

        throw Exception("Collaps could not find Russian HLS stream in player page")
    }

    private fun extractKodik(url: String): Video {
        val fullUrl = if (url.startsWith("//")) "https:$url" else url
        val req = Request.Builder()
            .url(fullUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .header("Referer", "https://kodik.info/")
            .build()

        val resp = client.newCall(req).execute()
        val html = resp.body?.string().orEmpty()

        // Match direct m3u8 link in Kodik player HTML
        val m3u8Regex = Pattern.compile("""https?://[^\s"'<>]+\.m3u8[^\s"'<>]*""").matcher(html)
        if (m3u8Regex.find()) {
            val stream = m3u8Regex.group(0)
            return Video(
                source = stream,
                headers = mapOf(
                    "Referer" to fullUrl,
                    "Origin" to "https://kodik.info",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                )
            )
        }

        // Try extracting via Kodik video API endpoint
        val dPattern = Pattern.compile("""urlParams\s*=\s*['"]?(\{[^'"]+\})""").matcher(html)
        val domain = if (fullUrl.contains("kodik.info")) "https://kodik.info" else "https://kodik.cc"
        
        // Parse params for /gvi call
        val typeMatcher = Pattern.compile("""videoInfo\.type\s*=\s*['"]([^'"]+)['"]""").matcher(html)
        val idMatcher = Pattern.compile("""videoInfo\.id\s*=\s*['"]([^'"]+)['"]""").matcher(html)
        val hashMatcher = Pattern.compile("""videoInfo\.hash\s*=\s*['"]([^'"]+)['"]""").matcher(html)

        if (typeMatcher.find() && idMatcher.find() && hashMatcher.find()) {
            val type = typeMatcher.group(1)
            val id = idMatcher.group(1)
            val hash = hashMatcher.group(1)

            val postBody = FormBody.Builder()
                .add("d", "kodik.info")
                .add("d_sign", "")
                .add("pd", "kodik.info")
                .add("pd_sign", "")
                .add("ref", "")
                .add("ref_sign", "")
                .add("type", type)
                .add("hash", hash)
                .add("id", id)
                .build()

            val gviReq = Request.Builder()
                .url("$domain/gvi")
                .post(postBody)
                .header("Referer", fullUrl)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val gviResp = client.newCall(gviReq).execute()
            val gviBody = gviResp.body?.string().orEmpty()
            if (gviBody.startsWith("{")) {
                val gviJson = JSONObject(gviBody)
                val links = gviJson.optJSONObject("links")
                if (links != null) {
                    val resList = listOf("1080", "720", "480", "360")
                    for (res in resList) {
                        val arr = links.optJSONArray(res)
                        if (arr != null && arr.length() > 0) {
                            val rawSrc = arr.getJSONObject(0).optString("src")
                            if (rawSrc.isNotBlank()) {
                                var fixedSrc = rawSrc
                                if (!fixedSrc.startsWith("http") && !fixedSrc.startsWith("//")) {
                                    // Kodik caesar/base64 cipher decode
                                    try {
                                        val b64 = String(java.util.Base64.getDecoder().decode(rawSrc))
                                        if (b64.startsWith("http") || b64.startsWith("//")) fixedSrc = b64
                                    } catch (_: Exception) {
                                        // 18-shift / rot13 decode
                                        fixedSrc = rawSrc.map { c ->
                                            when (c) {
                                                in 'a'..'z' -> ((c - 'a' + 8) % 26 + 'a'.code).toChar()
                                                in 'A'..'Z' -> ((c - 'A' + 8) % 26 + 'A'.code).toChar()
                                                else -> c
                                            }
                                        }.joinToString("")
                                    }
                                }
                                if (fixedSrc.startsWith("//")) fixedSrc = "https:$fixedSrc"
                                
                                return Video(
                                    source = fixedSrc,
                                    headers = mapOf(
                                        "Referer" to fullUrl,
                                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        throw Exception("Kodik extraction failed to find Russian video stream")
    }
}
