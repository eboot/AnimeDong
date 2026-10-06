package com.animedong.app.domain.billing

import kotlinx.coroutines.flow.StateFlow

/**
 * SATU-SATUNYA sumber status premium di app.
 * UI dan AdManager dilarang mengecek premium dengan cara lain.
 */
interface BillingRepository {
    val isPremium: StateFlow<Boolean>
    suspend fun isPremiumNow(): Boolean
    suspend fun refresh()

    /**
     * TODO: integrasi Google Play Billing dengan SKU premium asli.
     * Saat ini stub: jangan diklaim sudah bisa beli.
     */
    fun launchPurchase(activity: android.app.Activity)
}
