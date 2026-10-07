package com.animedong.app.presentation.profile

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.animedong.app.BuildConfig
import com.animedong.app.core.ui.SectionHeader
import com.animedong.app.data.auth.AuthManager
import com.animedong.app.data.local.PrefsManager
import com.animedong.app.domain.billing.BillingRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    authManager: AuthManager,
    billing: BillingRepository,
    prefs: PrefsManager
) {
    val user by authManager.user.collectAsState()
    val isPremium by billing.isPremium.collectAsState()
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as Activity
    var loginError by remember { mutableStateOf<String?>(null) }
    var loggingIn by remember { mutableStateOf(false) }

    val res1080p by prefs.res1080p.collectAsState(initial = true)
    val autoplayNext by prefs.autoplayNext.collectAsState(initial = true)
    val wifiOnly by prefs.wifiOnlyDownload.collectAsState(initial = true)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Kartu guest / user
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user?.photoUrl != null) {
                        AsyncImage(
                            model = user?.photoUrl.toString(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF8A00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF0E1218),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                user?.displayName ?: "Pengunjung Tamu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (user == null) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "GUEST",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (user == null) "Belum terhubung ke Akun Google"
                            else user?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Kartu login Google
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Masuk dengan Akun Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Tanpa ribet password, langsung sinkron",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Hubungkan akun Google Anda untuk mencadangkan daftar " +
                            "Favorit, Riwayat tontonan, serta menikmati fitur " +
                            "VIP di AnimeDong.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    if (user == null) {
                        Button(
                            onClick = {
                                loggingIn = true
                                loginError = null
                                scope.launch {
                                    authManager.signInWithGoogle()
                                        .onFailure {
                                            loginError = it.message ?: "Login gagal"
                                        }
                                    loggingIn = false
                                }
                            },
                            enabled = !loggingIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0E1218)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (loggingIn) "Membuka login..."
                                else "Lanjutkan dengan Google",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        loginError?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                authManager.signOut()
                                scope.launch { billing.refresh() }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Keluar") }
                    }
                }
            }
        }

        // Kartu VIP
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2113)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("★", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isPremium) "AnimeDong VIP Aktif"
                            else "Jadi AnimeDong VIP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isPremium) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    "Permanen",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // Keuntungan yang benar-benar didukung app (tanpa klaim palsu)
                    listOf(
                        "Streaming tanpa jeda iklan sama sekali",
                        "Sinkronisasi Favorit dan Riwayat antar perangkat",
                        "Prioritas fitur baru lebih dulu"
                    ).forEach {
                        Text(
                            "• $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE8E0C8),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (!isPremium) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { billing.launchPurchase(activity) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text("Upgrade ke Premium") }
                    }
                }
            }
        }

        // Pengaturan streaming
        item { SectionHeader(title = "Pengaturan Streaming") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    SettingToggle(
                        title = "Resolusi Default 1080p HD",
                        subtitle = "Putar otomatis dalam kualitas tertinggi",
                        checked = res1080p,
                        onChecked = { scope.launch { prefs.setRes1080p(it) } }
                    )
                    SettingToggle(
                        title = "Putar Episode Berikutnya",
                        subtitle = "Lanjut otomatis saat episode selesai",
                        checked = autoplayNext,
                        onChecked = { scope.launch { prefs.setAutoplayNext(it) } }
                    )
                    SettingToggle(
                        title = "Unduh Hanya Lewat Wi-Fi",
                        subtitle = "Hemat kuota data seluler ponsel",
                        checked = wifiOnly,
                        onChecked = { scope.launch { prefs.setWifiOnlyDownload(it) } },
                        showDivider = false
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "AnimeDong ${BuildConfig.VERSION_NAME} (Kotlin native)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onChecked,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        }
    }
}
