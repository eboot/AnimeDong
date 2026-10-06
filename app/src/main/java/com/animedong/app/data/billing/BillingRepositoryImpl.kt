package com.animedong.app.data.billing

import android.app.Activity
import android.widget.Toast
import com.animedong.app.data.remote.RemoteConfigManager
import com.animedong.app.domain.billing.BillingRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Status premium dibaca dari Firestore:
 * users/{uid}/subscription/current { isPremium, expiresAt, source }
 *
 * Guest (belum login) selalu dianggap tidak premium.
 */
class BillingRepositoryImpl(
    private val remoteConfig: RemoteConfigManager
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        // Ikuti perubahan login/logout.
        FirebaseAuth.getInstance().addAuthStateListener { refreshAsync() }
    }

    override suspend fun isPremiumNow(): Boolean = isPremium.first()

    override suspend fun refresh() {
        // Override untuk testing tanpa beli (Remote Config).
        if (remoteConfig.debugForcePremium) {
            _isPremium.value = true
            return
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _isPremium.value = false
            return
        }
        _isPremium.value = try {
            val snap = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("subscription").document("current")
                .get().await()
            val premium = snap.getBoolean("isPremium") == true
            val expiresAt = snap.getTimestamp("expiresAt")
            premium && (expiresAt == null || expiresAt > Timestamp.now())
        } catch (e: Exception) {
            false
        }
    }

    private fun refreshAsync() {
        scope.launch { refresh() }
    }

    override fun launchPurchase(activity: Activity) {
        // TODO: ganti dengan alur Google Play Billing (BillingClient +
        // queryProductDetails + launchBillingFlow dengan SKU premium asli).
        Toast.makeText(
            activity,
            "Pembelian premium segera hadir",
            Toast.LENGTH_SHORT
        ).show()
    }
}
