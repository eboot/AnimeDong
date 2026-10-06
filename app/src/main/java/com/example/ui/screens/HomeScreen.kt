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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimeCard
import com.example.ui.components.AnimeHorizontalCard
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AnimeDongBg
import com.example.ui.theme.AnimeDongCard
import com.example.ui.theme.AnimeDongCardBorder
import com.example.ui.theme.AnimeDongGold
import com.example.ui.theme.AnimeDongTextMuted
import com.example.ui.theme.AnimeDongTextPrimary
import com.example.ui.theme.AnimeDongTextSecondary
import com.example.viewmodel.AnimeDongViewModel

@Composable
fun HomeScreen(
    viewModel: AnimeDongViewModel,
    modifier: Modifier = Modifier
) {
    val ongoingList by viewModel.ongoingList.collectAsState()
    val completedList by viewModel.completedList.collectAsState()
    val isHomeLoading by viewModel.isHomeLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()

    val filteredOngoing = if (searchQuery.isNotBlank()) {
        ongoingList.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else {
        ongoingList
    }

    val filteredCompleted = if (searchQuery.isNotBlank()) {
        completedList.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else {
        completedList
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDongBg)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AnimeDong",
                            color = AnimeDongGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Streaming anime & donghua subtitle Indonesia",
                            color = AnimeDongTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadHomeData() },
                        modifier = Modifier.testTag("refresh_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang",
                            tint = AnimeDongGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text(
                            text = "Cari judul anime atau donghua...",
                            color = AnimeDongTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = AnimeDongGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = AnimeDongTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AnimeDongCard,
                        unfocusedContainerColor = AnimeDongCard,
                        focusedBorderColor = AnimeDongGold,
                        unfocusedBorderColor = AnimeDongCardBorder,
                        focusedTextColor = AnimeDongTextPrimary,
                        unfocusedTextColor = AnimeDongTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("search_text_field")
                )
            }
        }

        // Loading state
        if (isHomeLoading && ongoingList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AnimeDongGold, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Memuat daftar anime dari server...",
                            color = AnimeDongTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // Ongoing Anime Section
            item {
                SectionHeader(title = "Sedang Berlangsung (Ongoing)")
            }

            if (filteredOngoing.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Tidak ada judul yang sesuai dengan pencarian." else "Belum ada anime ongoing tersedia.",
                        color = AnimeDongTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOngoing, key = { it.id }) { anime ->
                            AnimeCard(
                                anime = anime,
                                onClick = { viewModel.openDetail(anime) },
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completed Anime Section
            item {
                SectionHeader(title = "Tamat (Completed)")
            }

            if (filteredCompleted.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Tidak ada anime tamat yang cocok." else "Belum ada anime tamat tersedia.",
                        color = AnimeDongTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(filteredCompleted, key = { it.id }) { anime ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        AnimeHorizontalCard(
                            animeId = anime.id,
                            title = anime.title,
                            poster = anime.poster,
                            subtitle = "${anime.episodes} • ${anime.latestReleaseDate.ifBlank { "Tamat" }}",
                            onClick = { viewModel.openDetail(anime) }
                        )
                    }
                }
            }
        }
    }
}
