package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AnimeDongBg
import com.example.ui.theme.AnimeDongCard
import com.example.ui.theme.AnimeDongCardBorder
import com.example.ui.theme.AnimeDongGold
import com.example.ui.theme.AnimeDongOrange
import com.example.ui.theme.AnimeDongSurface
import com.example.ui.theme.AnimeDongTextMuted
import com.example.ui.theme.AnimeDongTextPrimary
import com.example.ui.theme.AnimeDongTextSecondary
import com.example.viewmodel.AnimeDongViewModel

@Composable
fun ProfileScreen(
    viewModel: AnimeDongViewModel,
    modifier: Modifier = Modifier
) {
    val userSession by viewModel.userSession.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()
    val defaultQuality by viewModel.defaultQuality.collectAsState()
    val serverPickerEnabled by viewModel.serverPickerEnabled.collectAsState()

    var showAccountChooserDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDongBg)
            .testTag("profile_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AnimeDongCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userSession?.photoUrl != null) {
                            AsyncImage(
                                model = userSession?.photoUrl,
                                contentDescription = "Foto Profil",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, AnimeDongGold, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AnimeDongGold, AnimeDongOrange)
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
                                    color = AnimeDongTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (userSession != null) AnimeDongGold else AnimeDongCardBorder,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (userSession != null) "MEMBER" else "GUEST",
                                        color = if (userSession != null) Color.Black else AnimeDongTextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userSession?.email ?: "Mode tamu (dapat menonton tanpa login)",
                                color = AnimeDongTextMuted,
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
                                        text = "Google Sign-In Terhubung",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (userSession != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.signOut() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55EF4444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Keluar dari Akun",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Login with Google Banner (If not logged in)
        if (userSession == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_login_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131826)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoogleLogoIcon()
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Masuk dengan Akun Google",
                                    color = AnimeDongTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Simpan bookmark dan riwayat tontonan Anda",
                                    color = AnimeDongGold,
                                    fontSize = 11.sp
                                )
                            }
                        }

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

        // Firebase Remote Config Status Section
        item {
            SectionHeader(title = "Konfigurasi Server & Remote Config")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AnimeDongCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    RemoteConfigStatusRow(
                        title = "API Base URL (Remote Config)",
                        value = apiBaseUrl
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RemoteConfigStatusRow(
                        title = "Kualitas Default",
                        value = defaultQuality
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RemoteConfigStatusRow(
                        title = "Pemilih Server Diaktifkan",
                        value = if (serverPickerEnabled) "Ya (Aktif)" else "Tidak"
                    )
                }
            }
        }

        // About AnimeDong
        item {
            SectionHeader(title = "Tentang Aplikasi")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AnimeDongCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AnimeDongGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AnimeDong Android",
                                color = AnimeDongTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aplikasi streaming donghua subtitle Indonesia",
                                color = AnimeDongTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

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
fun RemoteConfigStatusRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AnimeDongTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = AnimeDongGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
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
                    text = "Pilih akun Google Anda untuk dihubungkan:",
                    color = AnimeDongTextSecondary,
                    fontSize = 12.sp
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAccountSelected(
                                defaultEmail,
                                defaultEmail.substringBefore("@").replace(".", " ")
                            )
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AnimeDongSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongGold.copy(alpha = 0.6f))
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
                                color = AnimeDongTextMuted,
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
                            color = AnimeDongGold,
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
                            focusedBorderColor = AnimeDongGold,
                            unfocusedBorderColor = AnimeDongCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (customEmail.isNotBlank()) {
                                onAccountSelected(
                                    customEmail.trim(),
                                    customEmail.substringBefore("@").replace(".", " ")
                                )
                            }
                        },
                        enabled = customEmail.contains("@"),
                        colors = ButtonDefaults.buttonColors(containerColor = AnimeDongGold),
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
                Text(text = "Batal", color = AnimeDongTextMuted)
            }
        }
    )
}
