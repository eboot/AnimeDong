package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DongHiveBg
import com.example.ui.theme.DongHiveCard
import com.example.ui.theme.DongHiveCardBorder
import com.example.ui.theme.DongHiveGold
import com.example.ui.theme.DongHiveGoldDark
import com.example.ui.theme.DongHiveOrange
import com.example.ui.theme.DongHiveSurface
import com.example.ui.theme.DongHiveTextMuted
import com.example.ui.theme.DongHiveTextPrimary
import com.example.ui.theme.DongHiveTextSecondary
import com.example.viewmodel.DongHiveViewModel

@Composable
fun ProfileScreen(
    viewModel: DongHiveViewModel,
    modifier: Modifier = Modifier
) {
    val cacheSize by viewModel.cacheSizeMb.collectAsState()
    val autoNext by viewModel.autoNext.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()

    var wifiOnlyDownload by remember { mutableStateOf(true) }
    var highQualityStream by remember { mutableStateOf(true) }
    var showAccountChooserDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DongHiveBg)
            .testTag("profile_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        if (userSession?.photoUrl != null) {
                            AsyncImage(
                                model = userSession?.photoUrl,
                                contentDescription = "Foto Profil Google",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, DongHiveGold, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(DongHiveGold, DongHiveOrange)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userSession != null) {
                                    Text(
                                        text = userSession?.displayName?.take(1)?.uppercase() ?: "U",
                                        color = Color.Black,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userSession?.displayName ?: "Pengunjung Tamu",
                                    color = DongHiveTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (userSession != null) DongHiveGold else DongHiveCardBorder,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (userSession != null) "VIP" else "GUEST",
                                        color = if (userSession != null) Color.Black else DongHiveTextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userSession?.email ?: "Belum terhubung ke Akun Google",
                                color = DongHiveTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )

                            if (userSession != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Akun Google Terhubung",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons when Logged in vs Guest
                    if (userSession != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.syncUserData() },
                                colors = ButtonDefaults.buttonColors(containerColor = DongHiveSurface),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sync_user_data_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = DongHiveGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sinkron Data",
                                    color = DongHiveTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { viewModel.signOut() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55EF4444)),
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keluar",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // GOOGLE SIGN-IN PROMO CARD (When not logged in)
        if (userSession == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_login_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131826)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoogleLogoIcon()
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Masuk dengan Akun Google",
                                    color = DongHiveTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tanpa ribet password, langsung sinkron",
                                    color = DongHiveGold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Hubungkan akun Google Anda untuk mencadangkan daftar Favorit, Riwayat tontonan, serta menikmati fitur VIP di AnimeDong.",
                            color = DongHiveTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.signInWithGoogle(onFallbackNeeded = {
                                    showAccountChooserDialog = true
                                })
                            },
                            enabled = !isLoggingIn,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("google_sign_in_button")
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    color = Color(0xFF4285F4),
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Menghubungkan Akun...",
                                    color = Color(0xFF1E293B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                GoogleLogoIcon(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Lanjutkan dengan Google",
                                    color = Color(0xFF1E293B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // VIP Member Benefits Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveGold.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2E2211),
                                    Color(0xFF1E1824),
                                    Color(0xFF151722)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = DongHiveGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AnimeDong VIP Aktif",
                                    color = DongHiveGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = DongHiveGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Permanen",
                                    color = DongHiveGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "• Streaming Full HD 1080p 60fps tanpa jeda iklan\n• Server prioritas ultra cepat bebas buffering\n• Sinkronisasi daftar Favorit dan Riwayat antar perangkat\n• Akses episode baru 30 menit lebih awal",
                            color = DongHiveTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Streaming Settings Section
        item {
            SectionHeader(title = "Pengaturan Streaming", actionText = null)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.OndemandVideo,
                        title = "Resolusi Default 1080p HD",
                        subtitle = "Putar otomatis dalam kualitas tertinggi",
                        isChecked = highQualityStream,
                        onCheckedChange = { highQualityStream = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingToggleRow(
                        icon = Icons.Default.OndemandVideo,
                        title = "Putar Episode Berikutnya",
                        subtitle = "Lanjut otomatis saat episode selesai",
                        isChecked = autoNext,
                        onCheckedChange = { viewModel.toggleAutoNext() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingToggleRow(
                        icon = Icons.Default.Wifi,
                        title = "Unduh Hanya Lewat Wi-Fi",
                        subtitle = "Hemat kuota data seluler ponsel",
                        isChecked = wifiOnlyDownload,
                        onCheckedChange = { wifiOnlyDownload = it }
                    )
                }
            }
        }

        // Storage & Cache Section
        item {
            SectionHeader(title = "Penyimpanan & Cache", actionText = null)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(DongHiveSurface, CircleShape)
                                .border(1.dp, DongHiveCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = DongHiveGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pembersih Cache",
                                color = DongHiveTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ukuran Cache: $cacheSize",
                                color = DongHiveTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = DongHiveSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { viewModel.clearCache() }
                            .testTag("clear_cache_button")
                    ) {
                        Text(
                            text = "Bersihkan",
                            color = DongHiveGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Community and About Section
        item {
            SectionHeader(title = "Komunitas & Informasi", actionText = null)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    CommunityItem(
                        icon = Icons.Default.Public,
                        title = "Grup Telegram Komunitas",
                        subtitle = "Diskusi spoiler & rilis episode tercepat",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/animedong_id"))
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CommunityItem(
                        icon = Icons.Default.Shield,
                        title = "Kebijakan Privasi & Layanan",
                        subtitle = "Ketentuan penggunaan aplikasi AnimeDong",
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CommunityItem(
                        icon = Icons.Default.Info,
                        title = "Tentang AnimeDong",
                        subtitle = "Versi 2.4.0 (Build 2026.10) • Dibuat dengan cinta untuk pecinta Donghua & Anime",
                        onClick = { }
                    )
                }
            }
        }
    }

    // Google Account Chooser Fallback Dialog
    if (showAccountChooserDialog) {
        GoogleAccountChooserDialog(
            defaultEmail = "blogarticles73@gmail.com",
            onAccountSelected = { email, name ->
                viewModel.signInDirectly(email, name)
                showAccountChooserDialog = false
            },
            onDismiss = { showAccountChooserDialog = false }
        )
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            color = Color(0xFF4285F4),
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun GoogleAccountChooserDialog(
    defaultEmail: String,
    onAccountSelected: (email: String, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var customEmail by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B2030),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoogleLogoIcon()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pilih Akun Google",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pilih akun Google Anda untuk disinkronkan dengan AnimeDong:",
                    color = DongHiveTextSecondary,
                    fontSize = 12.sp
                )

                // Recommended Primary Account Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAccountSelected(defaultEmail, defaultEmail.substringBefore("@").replace(".", " ").capitalize())
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DongHiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = defaultEmail.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = defaultEmail.substringBefore("@"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = defaultEmail,
                                color = DongHiveTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (!useCustom) {
                    TextButton(
                        onClick = { useCustom = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "+ Gunakan Akun Lain",
                            color = DongHiveGold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Email Google Anda") },
                        placeholder = { Text("nama@gmail.com") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DongHiveGold,
                            unfocusedBorderColor = DongHiveCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (customEmail.isNotBlank()) {
                                onAccountSelected(
                                    customEmail.trim(),
                                    customEmail.substringBefore("@").replace(".", " ").capitalize()
                                )
                            }
                        },
                        enabled = customEmail.contains("@"),
                        colors = ButtonDefaults.buttonColors(containerColor = DongHiveGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Masuk dengan Akun Ini", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal", color = DongHiveTextMuted)
            }
        }
    )
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = DongHiveTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = DongHiveTextMuted,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = DongHiveGold
            )
        )
    }
}

@Composable
fun CommunityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DongHiveGold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = DongHiveTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = DongHiveTextMuted,
                    fontSize = 11.sp
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = DongHiveTextMuted,
            modifier = Modifier.size(12.dp)
        )
    }
}
