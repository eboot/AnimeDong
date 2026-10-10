package com.animedong.app.data.kurama

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Ekstrak direct video URL (HLS) dari halaman episode KuramaAnime
 * memakai WebView tersembunyi + injeksi JS.
 *
 * Alur di situs: halaman dimuat -> JS situs minta token -> #player dapat
 * atribut data-hlsSrc. Kita tinggal baca atribut itu, tanpa meniru
 * token flow yang ter-obfuscate.
 *
 * WebView dibuat di Main thread dan selalu di-destroy (berhasil/gagal).
 */
class KuramaStreamExtractor(private val appContext: Context) {

    private val desktopUa =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"

    private val probeJs = """
        (function() {
            var p = document.querySelector('#player');
            if (p) {
                if (p.dataset && p.dataset.hlsSrc) return p.dataset.hlsSrc;
                if (p.src) return p.src;
            }
            var v = document.querySelector('#animeVideoPlayer video, video');
            if (v) return v.src || v.currentSrc || null;
            return null;
        })()
    """.trimIndent()

    /**
     * @return URL HLS (.m3u8), atau null kalau timeout/gagal.
     * Dipanggil dari coroutine; WebView dibuat & dipakai di Main thread.
     */
    suspend fun extractHlsUrl(
        episodePageUrl: String,
        timeoutMs: Long = 60_000,
        pollMs: Long = 1_000
    ): String? = withContext(Dispatchers.Main) {
        withTimeoutOrNull(timeoutMs) {
            val webView = WebView(appContext)
            try {
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.settings.mediaPlaybackRequiresUserGesture = false
                webView.settings.userAgentString = desktopUa
                webView.webViewClient = WebViewClient()
                webView.loadUrl(episodePageUrl)
                while (true) {
                    delay(pollMs)
                    val found = webView.evalOnce(probeJs)
                    if (!found.isNullOrBlank()) return@withTimeoutOrNull found
                }
                @Suppress("UNREACHABLE_CODE")
                null
            } finally {
                webView.stopLoading()
                webView.destroy()
            }
        }
    }

    /** evaluateJavascript sekali; hasil "null"/kosong -> null. */
    private suspend fun WebView.evalOnce(script: String): String? =
        suspendCancellableCoroutine { cont ->
            evaluateJavascript(script) { raw ->
                val clean = raw?.trim()?.removeSurrounding("\"")
                if (cont.isActive) {
                    cont.resume(
                        if (clean.isNullOrBlank() || clean == "null") null else clean
                    )
                }
            }
        }
}
