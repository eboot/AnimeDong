package com.animedong.app.data.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.animedong.app.BuildConfig
import com.animedong.app.data.local.PrefsManager
import com.animedong.app.data.remote.RemoteConfigManager
import com.animedong.app.domain.billing.BillingRepository
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Pengelola iklan AdMob.
 *
 * Aturan premium (dari prompt):
 * - Non-premium: interstitial MAKSIMAL 1x per 30 menit, hanya di jeda
 *   natural (buka episode / kembali ke Beranda). DILARANG memotong video.
 * - Premium: NOL iklan.
 */
class AdManager(
    private val context: Context,
    private val remoteConfig: RemoteConfigManager,
    private val billing: BillingRepository,
    private val prefs: PrefsManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var interstitial: InterstitialAd? = null

    companion object {
        const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val INTERSTITIAL_INTERVAL_MS = 30 * 60 * 1000L
    }

    fun bannerUnitId(): String {
        if (BuildConfig.DEBUG) return TEST_BANNER
        return remoteConfig.admobBannerUnitId
    }

    private fun interstitialUnitId(): String {
        if (BuildConfig.DEBUG) return TEST_INTERSTITIAL
        return remoteConfig.admobInterstitialUnitId
    }

    fun preloadInterstitial() {
        val unitId = interstitialUnitId()
        if (unitId.isBlank() || interstitial != null) return
        InterstitialAd.load(
            context, unitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(e: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }

    /**
     * Tampilkan interstitial jika layak. [onDone] SELALU dipanggil
     * (tampil atau tidak) agar alur navigasi tidak macet.
     */
    fun maybeShowInterstitial(activity: Activity, onDone: () -> Unit) {
        scope.launch {
            val premium = billing.isPremiumNow()
            val adsEnabled = remoteConfig.adsEnabled
            val last = prefs.getLastInterstitialAt()
            val due = System.currentTimeMillis() - last >= INTERSTITIAL_INTERVAL_MS
            val ad = interstitial
            if (!adsEnabled || premium || !due || ad == null) {
                if (ad == null) preloadInterstitial()
                onDone()
                return@launch
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    preloadInterstitial()
                    scope.launch {
                        prefs.setLastInterstitialAt(System.currentTimeMillis())
                    }
                    onDone()
                }

                override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) {
                    interstitial = null
                    preloadInterstitial()
                    onDone()
                }
            }
            ad.show(activity)
        }
    }
}

/** Banner AdMob. Tampilkan hanya jika [show] true (non-premium & ads nyala). */
@Composable
fun BannerAd(
    show: Boolean,
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    if (!show) return
    val unitId = adManager.bannerUnitId()
    if (unitId.isBlank()) return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
