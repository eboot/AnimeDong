package com.animedong.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.animedong.app.core.theme.AnimeDongTheme
import com.animedong.app.data.dns.DnsChecker
import com.animedong.app.presentation.navigation.AnimeDongNavGraph

class MainActivity : ComponentActivity() {

    private var dnsBlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35 memaksa edge-to-edge; Scaffold di NavGraph sudah
        // mengonsumsi insets, jadi konten tidak tertutup status/nav bar.
        enableEdgeToEdge()
        val container = (application as AnimeDongApp).container
        // Deep link dari notifikasi FCM (extra "episodeId").
        val startEpisodeId = intent.getStringExtra("episodeId")
        setContent {
            AnimeDongTheme {
                AnimeDongNavGraph(container, startEpisodeId)
                if (dnsBlocked) {
                    DnsBlockedDialog(
                        onOpenSettings = { DnsChecker.openPrivateDnsSettings(this) },
                        onRestart = { DnsChecker.restartApp(this) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Cek ulang tiap kembali ke app (user bisa ubah DNS dari Settings).
        dnsBlocked = DnsChecker.isPrivateDnsActive(this)
    }
}

/** Dialog blokir Private DNS: TIDAK bisa di-dismiss, 2 tombol sesuai spek. */
@Composable
private fun DnsBlockedDialog(
    onOpenSettings: () -> Unit,
    onRestart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* sengaja kosong: tidak bisa di-dismiss */ },
        title = { Text("Private DNS Terdeteksi") },
        text = {
            Text(
                "AnimeDong tidak mendukung Private DNS / DNS kustom karena " +
                    "dapat memblokir iklan dan mengganggu streaming. " +
                    "Matikan dulu untuk melanjutkan."
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("Buka Pengaturan DNS") }
        },
        dismissButton = {
            TextButton(onClick = onRestart) { Text("Restart AnimeDong") }
        }
    )
}
