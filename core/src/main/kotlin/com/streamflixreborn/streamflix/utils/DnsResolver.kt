package com.streamflixreborn.streamflix.utils

import com.streamflixreborn.streamflix.compat.Log
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetAddress
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object DnsResolver : Dns {
    private const val TAG = "DnsResolver"

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
    })
    private val sslContext = SSLContext.getInstance("TLS").apply { init(null, trustAllCerts, SecureRandom()) }
    private val trustManager = trustAllCerts[0] as X509TrustManager

    private var client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier { _, _ -> true }
        .build()

    private var _url: String = UserPreferences.dohProviderUrl
    private var _internalDoh: Dns = buildDoh(_url)

    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            _internalDoh.lookup(hostname)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve $hostname: ${e.message}")
            if (_internalDoh === Dns.SYSTEM) throw e
            Dns.SYSTEM.lookup(hostname)
        }
    }

    val doh: Dns get() = this

    @Synchronized
    fun setDnsUrl(newUrl: String) {
        if (newUrl != _url) {
            _url = newUrl
            _internalDoh = buildDoh(_url)
        }
    }

    @Synchronized
    private fun buildDoh(url: String): Dns {
        return if (url.isNotEmpty()) {
            try { DnsOverHttps.Builder().client(client).url(url.toHttpUrl()).build() }
            catch (e: Exception) { Log.e(TAG, "DoH error: ${e.message}"); Dns.SYSTEM }
        } else Dns.SYSTEM
    }
}
