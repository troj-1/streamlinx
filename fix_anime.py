import re

with open('providers/src/main/kotlin/com/streamflixreborn/streamflix/providers/AnimeOnlineNinjaProvider.kt', 'r', encoding='utf-8') as f:
    c = f.read()

# 1. Imports
c = re.sub(r'(?m)^import android\..*\n', '', c)
c = re.sub(r'(?m)^import com\.streamflixreborn\.streamflix\.StreamFlixApp.*\n', '', c)
c = c.replace('import android.webkit.CookieManager', 'import okhttp3.HttpUrl.Companion.toHttpUrlOrNull')

# 2. init
c = c.replace('fun init(context: Context)', 'fun init(context: Any?)')
c = c.replace('AnimeOnlineNinjaCronetClient.init(context)', '')

# 3. UserPreferences preferred server
c = re.sub(r'val preferred = UserPreferences.*?\.uppercase\(\)', 'val preferred = ""', c, flags=re.DOTALL)

# 4. CookieManager logic
c = re.sub(r'private fun syncClearanceCookieState\(\): String\? \{[\s\S]*?return liveCookieHeader\n    \}',
    'private fun syncClearanceCookieState(): String? {\n        return com.streamflixreborn.streamflix.utils.NetworkClient.cookieJar.loadForRequest(baseUrl.toHttpUrlOrNull()!!).joinToString("; ")\n    }', c)
c = c.replace('fun hasCurrentClearanceCookie(): Boolean {\n        return !clearanceToken(syncClearanceCookieState()).isNullOrBlank()\n    }', 'fun hasCurrentClearanceCookie(): Boolean = !currentClearanceCookie().isNullOrBlank()')

# 5. resolveServers
c = re.sub(r'val response = AnimeOnlineNinjaCronetClient\.get\(\n            context = StreamFlixApp\.instance,([\s\S]*?)useCache = false,\n        \)\n        if \(!response\.isSuccessful\) \{\n            throw IllegalStateException\("Cronet embed HTTP \$\{response\.statusCode\}: \"\)\n        \}\n        val document = Jsoup\.parse\(response\.bodyAsString\(\), response\.finalUrl\)',
    r'val response = AnimeOnlineNinjaCronetClient.get(\1)\n        if (response.isNullOrBlank()) {\n            throw IllegalStateException("Cronet embed HTTP failed: ")\n        }\n        val document = Jsoup.parse(response, embedUrl)', c)

# 6. extractServers
c = re.sub(r'val response = AnimeOnlineNinjaCronetClient\.get\(\n            context = StreamFlixApp\.instance,([\s\S]*?)useCache = false,\n        \)\n        if \(!response\.isSuccessful\) \{\n            throw IllegalStateException\("Cronet embed HTTP \$\{response\.statusCode\}: \"\)\n        \}\n        val document = Jsoup\.parse\(response\.bodyAsString\(\), response\.finalUrl\)',
    r'val response = AnimeOnlineNinjaCronetClient.get(\1)\n        if (response.isNullOrBlank()) {\n            throw IllegalStateException("Cronet embed HTTP failed: ")\n        }\n        val document = Jsoup.parse(response, embedUrl)', c)

# 7. ifBlank fix in extractServers
c = c.replace('val serverUrl = normalizeExternalUrl(\n                    element.absUrl("src").ifBlank { element.attr("src") },\n                    embedUrl,\n                ) ?: return@forEachIndexed',
    'val srcAttr = element.absUrl("src")\n                val fallbackAttr = element.attr("src")\n                val finalSrc = if (srcAttr.isBlank()) fallbackAttr else srcAttr\n                val serverUrl = normalizeExternalUrl(finalSrc, embedUrl) ?: return@forEachIndexed')

# 8. fetchDocumentDirect
c = re.sub(r'val response = AnimeOnlineNinjaCronetClient\.get\(\n            context = StreamFlixApp\.instance,([\s\S]*?)useCache = false,\n        \)\n        val body = response\.bodyAsString\(\)\n\n        // Cloudflare may append[\s\S]*?return null\n    \}',
    r'val body = AnimeOnlineNinjaCronetClient.get(\1)\n        if (body.isNullOrBlank()) return null\n        if (hasUsableSiteContent(body, url)) {\n            return Jsoup.parse(body, url)\n        }\n        if (requiresClearance(body) || url.contains("/cdn-cgi/", ignoreCase = true)) {\n            throw Exception("AnimeOnline Ninja Cloudflare challenge detected for ")\n        }\n        return null\n    }', c)

with open('providers/src/main/kotlin/com/streamflixreborn/streamflix/providers/AnimeOnlineNinjaProvider.kt', 'w', encoding='utf-8') as f:
    f.write(c)