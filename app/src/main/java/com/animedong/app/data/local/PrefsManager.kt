package com.animedong.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefs by preferencesDataStore("animedong_prefs")

/** Penyimpanan key-value ringan (pengaturan, throttle iklan, dsb). */
class PrefsManager(private val context: Context) {

    private val KEY_LAST_INTERSTITIAL = longPreferencesKey("last_interstitial_at")
    private val KEY_RES_1080P = booleanPreferencesKey("res_1080p")
    private val KEY_AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
    private val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_download")

    suspend fun getLastInterstitialAt(): Long =
        context.prefs.data.map { it[KEY_LAST_INTERSTITIAL] ?: 0L }.first()

    suspend fun setLastInterstitialAt(timestampMs: Long) {
        context.prefs.edit { it[KEY_LAST_INTERSTITIAL] = timestampMs }
    }

    // Pengaturan streaming (layar Profil)
    val res1080p: Flow<Boolean> =
        context.prefs.data.map { it[KEY_RES_1080P] ?: true }
    val autoplayNext: Flow<Boolean> =
        context.prefs.data.map { it[KEY_AUTOPLAY_NEXT] ?: true }
    val wifiOnlyDownload: Flow<Boolean> =
        context.prefs.data.map { it[KEY_WIFI_ONLY] ?: true }

    suspend fun setRes1080p(v: Boolean) {
        context.prefs.edit { it[KEY_RES_1080P] = v }
    }

    suspend fun setAutoplayNext(v: Boolean) {
        context.prefs.edit { it[KEY_AUTOPLAY_NEXT] = v }
    }

    suspend fun setWifiOnlyDownload(v: Boolean) {
        context.prefs.edit { it[KEY_WIFI_ONLY] = v }
    }
}
