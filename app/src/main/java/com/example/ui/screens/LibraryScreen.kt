package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.EmptyState
import com.example.ui.theme.DongHiveBg
import com.example.ui.theme.DongHiveCard
import com.example.ui.theme.DongHiveCardBorder
import com.example.ui.theme.DongHiveGold
import com.example.ui.theme.DongHiveOrange
import com.example.ui.theme.DongHiveSurface
import com.example.ui.theme.DongHiveTextMuted
import com.example.ui.theme.DongHiveTextPrimary
import com.example.ui.theme.DongHiveTextSecondary
import com.example.viewmodel.DongHiveViewModel

@Composable
fun LibraryScreen(
    viewModel: DongHiveViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.libraryTab.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val history by viewModel.history.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    val tabTitles = listOf(
        "Favorit (${favorites.size})",
        "Riwayat (${history.size})",
        "Unduhan (${downloads.size})"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DongHiveBg)
            .testTag("library_screen_content")
    ) {
        // Library Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Koleksi Saya",
                    color = DongHiveTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Daftar tontonan, riwayat, dan video offline",
                    color = DongHiveTextMuted,
                    fontSize = 11.sp
                )
            }

            if (selectedTab == 1 && history.isNotEmpty()) {
                Text(
                    text = "Hapus Semua",
                    color = DongHiveGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.clearAllHistory() }
                )
            }
        }

        // Custom M3 Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DongHiveSurface,
            contentColor = DongHiveGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = DongHiveGold,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.setLibraryTab(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) DongHiveGold else DongHiveTextSecondary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                // TAB 0: FAVORITES
                0 -> {
                    if (favorites.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Bookmark,
                            title = "Belum Ada Donghua Favorit",
                            subtitle = "Tekan tombol bookmark pada donghua yang kamu sukai untuk menyimpannya di sini."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(favorites) { fav ->
                                val donghua = viewModel.repository.getDonghuaById(fav.donghuaId) ?: return@items
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.openDetail(donghua) },
                                    colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fav.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = fav.title,
                                            modifier = Modifier
                                                .size(width = 65.dp, height = 85.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fav.title,
                                                color = DongHiveTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = fav.chineseTitle,
                                                color = DongHiveGold,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Episode ${fav.latestEpisode} • ${fav.status} • ★ ${fav.rating}",
                                                color = DongHiveTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.toggleFavorite(donghua) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = DongHiveTextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 1: WATCH HISTORY
                1 -> {
                    if (history.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.History,
                            title = "Belum Ada Riwayat Nonton",
                            subtitle = "Episode yang kamu tonton akan otomatis tercatat dan dapat dilanjutkan kapan saja."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history) { item ->
                                val donghua = viewModel.repository.getDonghuaById(item.donghuaId) ?: return@items
                                val eps = viewModel.repository.getEpisodesForDonghua(donghua)
                                val targetEp = eps.find { it.episodeNumber == item.episodeNumber } ?: eps.firstOrNull()

                                val progressFraction = if (item.durationMs > 0) {
                                    (item.progressMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
                                } else 0.5f

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (targetEp != null) {
                                                viewModel.openPlayer(donghua, targetEp, item.progressMs)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 75.dp, height = 90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(item.coverUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = item.donghuaTitle,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color(0x99000000), CircleShape)
                                                    .align(Alignment.Center),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = DongHiveGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.donghuaTitle,
                                                color = DongHiveTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Episode ${item.episodeNumber}",
                                                color = DongHiveGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { progressFraction },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = DongHiveGold,
                                                trackColor = DongHiveCardBorder
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Lanjutkan menonton",
                                                color = DongHiveTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeHistoryItem(item.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = DongHiveTextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: DOWNLOADS
                2 -> {
                    if (downloads.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.DownloadDone,
                            title = "Belum Ada Episode Diunduh",
                            subtitle = "Unduh episode saat terhubung ke Wi-Fi untuk menonton tanpa kuota internet."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(downloads) { dl ->
                                val donghua = viewModel.repository.getDonghuaById(dl.donghuaId)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (donghua != null) {
                                                val eps = viewModel.repository.getEpisodesForDonghua(donghua)
                                                val target = eps.find { it.episodeNumber == dl.episodeNumber } ?: eps.first()
                                                viewModel.openPlayer(donghua, target)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(dl.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = dl.donghuaTitle,
                                            modifier = Modifier
                                                .size(width = 65.dp, height = 80.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = dl.donghuaTitle,
                                                color = DongHiveTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Episode ${dl.episodeNumber} • ${dl.quality}",
                                                color = DongHiveGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tersimpan offline • %.1f MB".format(dl.fileSizeMb),
                                                color = DongHiveTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeDownloadItem(dl.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = DongHiveTextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
