package com.animedong.app.data.remote

import com.animedong.app.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Satu pintu baca Firebase Remote Config.
 * Nilai default di bawah dipakai saat fetch gagal / belum pernah fetch.
 */
class RemoteConfigManager {

    private val rc: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        rc.setConfigSettingsAsync(remoteConfigSettings {
            // 1 jam saat debug, 12 jam saat release (sesuai prompt).
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 3600 else 43200
        })
        rc.setDefaultsAsync(
            mapOf(
                "api_base_url" to "https://www.sankavollerei.web.id",
                "donghua_api_base_url" to "http://168.110.213.108/donghua",
                "default_quality" to "480p",
                "server_picker_enabled" to true,
                "min_app_version" to 1L,
                "ads_enabled" to true,
                "admob_banner_unit_id" to "",
                "admob_interstitial_unit_id" to "",
                "blocked_dns_hosts" to "",
                "debug_force_premium" to false
            )
        )
    }

    /** Fetch + activate. Return true jika ada nilai baru yang aktif. */
    suspend fun fetchAndActivate(): Boolean = try {
        rc.fetchAndActivate().await()
    } catch (e: Exception) {
        false
    }

    val apiBaseUrl: String
        get() = rc.getString("api_base_url")
            .ifBlank { "https://www.sankavollerei.web.id" }
    val donghuaApiBaseUrl: String
        get() = rc.getString("donghua_api_base_url")
            .ifBlank { "http://168.110.213.108/donghua" }
    val defaultQuality: String get() = rc.getString("default_quality").ifBlank { "480p" }
    val serverPickerEnabled: Boolean get() = rc.getBoolean("server_picker_enabled")
    val minAppVersion: Long get() = rc.getLong("min_app_version")
    val adsEnabled: Boolean get() = rc.getBoolean("ads_enabled")
    val admobBannerUnitId: String get() = rc.getString("admob_banner_unit_id")
    val admobInterstitialUnitId: String get() = rc.getString("admob_interstitial_unit_id")
    val blockedDnsHosts: List<String>
        get() = rc.getString("blocked_dns_hosts")
            .split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    val debugForcePremium: Boolean get() = rc.getBoolean("debug_force_premium")
}
