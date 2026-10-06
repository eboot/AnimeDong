package com.example.ui.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.system.exitProcess

object PrivateDnsChecker {

    fun isPrivateDnsActive(context: Context, blockedHosts: List<String> = emptyList()): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val mode = Settings.Global.getString(context.contentResolver, "private_dns_mode")
                // "hostname" indicates custom private DNS provider is specified
                if (mode != null && mode.equals("hostname", ignoreCase = true)) {
                    return true
                }
                val specifier = Settings.Global.getString(context.contentResolver, "private_dns_specifier")
                if (!specifier.isNullOrBlank() && blockedHosts.isNotEmpty()) {
                    if (blockedHosts.any { specifier.contains(it, ignoreCase = true) }) {
                        return true
                    }
                }
            } catch (e: Exception) {
                // If setting cannot be read, default to allowing
                return false
            }
        }
        return false
    }

    fun openDnsSettings(context: Context) {
        val dnsIntent = Intent("android.settings.PRIVATE_DNS_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (dnsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(dnsIntent)
        } else {
            val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val pendingIntent = PendingIntent.getActivity(
                context,
                123456,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.set(
                AlarmManager.RTC,
                System.currentTimeMillis() + 300,
                pendingIntent
            )
        }
        exitProcess(0)
    }
}

@Composable
fun PrivateDnsBlockedDialog(
    onOpenSettings: () -> Unit,
    onRestart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Cannot be dismissed */ },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF1B2030),
        title = {
            Text(
                text = "Private DNS Terdeteksi",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "AnimeDong tidak mendukung Private DNS / DNS kustom karena dapat memblokir iklan dan mengganggu streaming. Matikan dulu untuk melanjutkan.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Buka Pengaturan DNS",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Restart AnimeDong",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}
