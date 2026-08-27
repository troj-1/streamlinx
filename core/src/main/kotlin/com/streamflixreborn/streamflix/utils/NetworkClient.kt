package com.streamflixreborn.streamflix.utils

import com.streamflixreborn.streamflix.compat.Log
import okhttp3.ConnectionSpec
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkClient {
    private const val TAG = "NetworkClient"
    const val USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"

    // In-memory cookie jar for desktop (replaces Android CookieManager)
    val cookieJar = object : CookieJar {
        private val store = mutableMapOf<String, MutableList<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store.getOrPut(url.host) { mutableListOf() }.apply {
                removeAll { existing -> cookies.any { it.name == existing.name } }
                addAll(cookies)
            }
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return store[url.host]?.filter { !it.expiresAt.let { exp -> exp < System.currentTimeMillis() } } ?: emptyList()
        }
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor { message -> Log.d(TAG, "[OkHttp] $message") }
            .apply { level = HttpLoggingInterceptor.Level.BASIC }
    }

    val default: OkHttpClient by lazy { buildClient(DnsResolver.doh) }
    val systemDns: OkHttpClient by lazy { buildClient(Dns.SYSTEM) }
    val noRedirects: OkHttpClient by lazy { buildClient(DnsResolver.doh) { it.followRedirects(false).followSslRedirects(false) } }

    val trustAll: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS").apply { init(null, trustAllCerts, SecureRandom()) }
        buildClient(DnsResolver.doh) {
            it.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
              .hostnameVerifier { _, _ -> true }
        }
    }

    private fun buildClient(dns: Dns, customizer: ((OkHttpClient.Builder) -> Unit)? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                val isCorsRequest = original.header("Sec-Fetch-Mode") == "cors" ||
                        original.header("Sec-Fetch-Dest") == "empty"
                if (original.header("User-Agent") == null)
                    requestBuilder.header("User-Agent", USER_AGENT)
                if (original.header("Accept") == null)
                    requestBuilder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                if (original.header("Accept-Language") == null)
                    requestBuilder.header("Accept-Language", "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7")
                if (!isCorsRequest && original.header("Sec-Fetch-Dest") == null)
                    requestBuilder.header("Sec-Fetch-Dest", "document")
                if (!isCorsRequest && original.header("Sec-Fetch-Mode") == null)
                    requestBuilder.header("Sec-Fetch-Mode", "navigate")
                if (!isCorsRequest && original.header("Sec-Fetch-Site") == null)
                    requestBuilder.header("Sec-Fetch-Site", "none")
                if (!isCorsRequest && original.header("Upgrade-Insecure-Requests") == null)
                    requestBuilder.header("Upgrade-Insecure-Requests", "1")
                chain.proceed(requestBuilder.build())
            }
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .dns(dns)

        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .build()
        builder.connectionSpecs(listOf(spec, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))

        customizer?.invoke(builder)
        return builder.build()
    }
}
