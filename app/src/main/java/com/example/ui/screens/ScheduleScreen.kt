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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DongHiveBadgeOngoing
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
fun ScheduleScreen(
    viewModel: DongHiveViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()
    val scheduleItems = viewModel.repository.getScheduleForDay(selectedDay)
    val reminderMap = remember { mutableStateMapOf<String, Boolean>() }

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DongHiveBg)
            .testTag("schedule_screen_content")
    ) {
        // Schedule Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(DongHiveGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Jadwal Rilis Mingguan",
                    color = DongHiveTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Update episode baru tepat waktu sesuai WIB",
                    color = DongHiveTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Days of week selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { day ->
                val isSelected = selectedDay == day
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) DongHiveGold else DongHiveCard,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                    modifier = Modifier
                        .clickable { viewModel.updateScheduleDay(day) }
                        .testTag("schedule_day_$day")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.take(3).uppercase(),
                            color = if (isSelected) Color.Black else DongHiveTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = day,
                            color = if (isSelected) Color.Black else DongHiveTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SectionHeader(
            title = "Jadwal Tayang: $selectedDay (${scheduleItems.size} Judul)",
            actionText = null
        )

        if (scheduleItems.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Schedule,
                title = "Tidak Ada Rilis Hari Ini",
                subtitle = "Pilih hari lain di atas untuk melihat jadwal rilis donghua favoritmu."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scheduleItems) { donghua ->
                    val isReminderSet = reminderMap[donghua.id] ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.openDetail(donghua) }
                            .testTag("schedule_item_${donghua.id}"),
                        colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Time Badge Column
                            Column(
                                modifier = Modifier.width(68.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = DongHiveSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                                ) {
                                    Text(
                                        text = donghua.releaseTime,
                                        color = DongHiveGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Rilis Baru",
                                    color = DongHiveBadgeOngoing,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Poster
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(donghua.coverUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = donghua.title,
                                modifier = Modifier
                                    .size(width = 65.dp, height = 85.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = donghua.title,
                                    color = DongHiveTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = donghua.chineseTitle,
                                    color = DongHiveGold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Episode ${donghua.latestEpisode} • ${donghua.genres.firstOrNull() ?: "Aksi"}",
                                    color = DongHiveTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            // Notification & Play Action
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                IconButton(
                                    onClick = {
                                        reminderMap[donghua.id] = !isReminderSet
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isReminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = "Reminder",
                                        tint = if (isReminderSet) DongHiveGold else DongHiveTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val eps = viewModel.repository.getEpisodesForDonghua(donghua)
                                        if (eps.isNotEmpty()) {
                                            viewModel.openPlayer(donghua, eps.first())
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(DongHiveGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Tonton",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
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
