package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemoteConfigManager private constructor(context: Context) {

    private val remoteConfig: FirebaseRemoteConfig

    private val _apiBaseUrl = MutableStateFlow(DEFAULT_API_BASE_URL)
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    private val _defaultQuality = MutableStateFlow("480p")
    val defaultQuality: StateFlow<String> = _defaultQuality.asStateFlow()

    private val _serverPickerEnabled = MutableStateFlow(true)
    val serverPickerEnabled: StateFlow<Boolean> = _serverPickerEnabled.asStateFlow()

    private val _minAppVersion = MutableStateFlow(1L)
    val minAppVersion: StateFlow<Long> = _minAppVersion.asStateFlow()

    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    private val _admobBannerUnitId = MutableStateFlow("ca-app-pub-3940256099942544/6300978111")
    val admobBannerUnitId: StateFlow<String> = _admobBannerUnitId.asStateFlow()

    private val _admobInterstitialUnitId = MutableStateFlow("ca-app-pub-3940256099942544/1033173712")
    val admobInterstitialUnitId: StateFlow<String> = _admobInterstitialUnitId.asStateFlow()

    private val _blockedDnsHosts = MutableStateFlow("")
    val blockedDnsHosts: StateFlow<String> = _blockedDnsHosts.asStateFlow()

    private val _debugForcePremium = MutableStateFlow(false)
    val debugForcePremium: StateFlow<Boolean> = _debugForcePremium.asStateFlow()

    init {
        // Ensure FirebaseApp initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        remoteConfig = FirebaseRemoteConfig.getInstance()

        // 1 hour in debug (3600s), 12 hours in release (43200s)
        val minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 3600L else 43200L

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(minimumFetchIntervalInSeconds)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default values required by specification
        val defaults: Map<String, Any> = mapOf(
            KEY_API_BASE_URL to DEFAULT_API_BASE_URL,
            KEY_DEFAULT_QUALITY to "480p",
            KEY_SERVER_PICKER_ENABLED to true,
            KEY_MIN_APP_VERSION to 1L,
            KEY_ADS_ENABLED to true,
            KEY_ADMOB_BANNER_UNIT_ID to "ca-app-pub-3940256099942544/6300978111",
            KEY_ADMOB_INTERSTITIAL_UNIT_ID to "ca-app-pub-3940256099942544/1033173712",
            KEY_BLOCKED_DNS_HOSTS to "",
            KEY_DEBUG_FORCE_PREMIUM to false
        )
        remoteConfig.setDefaultsAsync(defaults)
        applyConfigValues()
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit = {}) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RemoteConfigManager", "Fetch and activate succeeded")
                    applyConfigValues()
                    onComplete(true)
                } else {
                    Log.w("RemoteConfigManager", "Fetch failed, using defaults or cached values", task.exception)
                    applyConfigValues()
                    onComplete(false)
                }
            }
    }

    private fun applyConfigValues() {
        val url = remoteConfig.getString(KEY_API_BASE_URL)
        if (url.isNotBlank()) {
            _apiBaseUrl.value = url.trimEnd('/')
        }
        _defaultQuality.value = remoteConfig.getString(KEY_DEFAULT_QUALITY)
        _serverPickerEnabled.value = remoteConfig.getBoolean(KEY_SERVER_PICKER_ENABLED)
        _minAppVersion.value = remoteConfig.getLong(KEY_MIN_APP_VERSION)
        _adsEnabled.value = remoteConfig.getBoolean(KEY_ADS_ENABLED)
        _admobBannerUnitId.value = remoteConfig.getString(KEY_ADMOB_BANNER_UNIT_ID)
        _admobInterstitialUnitId.value = remoteConfig.getString(KEY_ADMOB_INTERSTITIAL_UNIT_ID)
        _blockedDnsHosts.value = remoteConfig.getString(KEY_BLOCKED_DNS_HOSTS)
        _debugForcePremium.value = remoteConfig.getBoolean(KEY_DEBUG_FORCE_PREMIUM)
    }

    companion object {
        const val DEFAULT_API_BASE_URL = "https://www.sankavollerei.web.id"
        const val KEY_API_BASE_URL = "api_base_url"
        const val KEY_DEFAULT_QUALITY = "default_quality"
        const val KEY_SERVER_PICKER_ENABLED = "server_picker_enabled"
        const val KEY_MIN_APP_VERSION = "min_app_version"
        const val KEY_ADS_ENABLED = "ads_enabled"
        const val KEY_ADMOB_BANNER_UNIT_ID = "admob_banner_unit_id"
        const val KEY_ADMOB_INTERSTITIAL_UNIT_ID = "admob_interstitial_unit_id"
        const val KEY_BLOCKED_DNS_HOSTS = "blocked_dns_hosts"
        const val KEY_DEBUG_FORCE_PREMIUM = "debug_force_premium"

        @Volatile
        private var INSTANCE: RemoteConfigManager? = null

        fun getInstance(context: Context): RemoteConfigManager {
            return INSTANCE ?: synchronized(this) {
                val instance = RemoteConfigManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
