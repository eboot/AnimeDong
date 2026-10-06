package com.animedong.app

import android.app.Application
import com.animedong.app.di.AppContainer
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AnimeDongApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // App Check dipasang SEBELUM layanan Firebase lain dipakai.
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        // Crash report hanya dikirim di build release agar dashboard tidak kotor.
        FirebaseCrashlytics.getInstance()
            .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        MobileAds.initialize(this) {}

        container = AppContainer(this)

        appScope.launch {
            val changed = container.remoteConfig.fetchAndActivate()
            if (changed) container.refreshApiClient()
            container.adManager.preloadInterstitial()
            container.billingRepository.refresh()
        }
    }
}
