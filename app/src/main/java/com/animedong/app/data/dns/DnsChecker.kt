package com.animedong.app.data.dns

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

/**
 * Larangan Private DNS / DNS kustom.
 * Alasan: DNS kustom dapat memblokir iklan dan merusak koneksi
 * ke server streaming, jadi tidak diizinkan di AnimeDong.
 */
object DnsChecker {

    /**
     * true jika user memakai Private DNS kustom.
     * Nilai "private_dns_mode": "off" | "opportunistic" | "hostname".
     * Hanya "hostname" yang berarti user mengisi hostname DNS sendiri.
     */
    fun isPrivateDnsActive(context: Context): Boolean {
        val mode = Settings.Global.getString(
            context.contentResolver, "private_dns_mode"
        )
        return mode == "hostname"
    }

    /** Buka pengaturan Private DNS. Coba intent langsung, fallback ke Network & Internet. */
    fun openPrivateDnsSettings(context: Context) {
        val direct = Intent("android.settings.PRIVATE_DNS_SETTINGS")
        // WAJIB verifikasi di HP asli (Android 9+): tidak semua ROM
        // merespons action di atas, makanya ada fallback.
        val intent = if (direct.resolveActivity(context.packageManager) != null) {
            direct
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Restart penuh proses aplikasi. */
    fun restartApp(context: Context) {
        val launch = context.packageManager
            .getLaunchIntentForPackage(context.packageName) ?: return
        val pending = PendingIntent.getActivity(
            context, 0, launch,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .set(AlarmManager.RTC, System.currentTimeMillis() + 500, pending)
        Process.killProcess(Process.myPid())
    }
}
