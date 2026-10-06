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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.AnimeHorizontalCard
import com.example.ui.components.EmptyState
import com.example.ui.theme.AnimeDongBg
import com.example.ui.theme.AnimeDongCard
import com.example.ui.theme.AnimeDongCardBorder
import com.example.ui.theme.AnimeDongGold
import com.example.ui.theme.AnimeDongSurface
import com.example.ui.theme.AnimeDongTextMuted
import com.example.ui.theme.AnimeDongTextPrimary
import com.example.ui.theme.AnimeDongTextSecondary
import com.example.viewmodel.AnimeDongViewModel

@Composable
fun ScheduleScreen(
    viewModel: AnimeDongViewModel,
    modifier: Modifier = Modifier
) {
    val scheduleList by viewModel.scheduleList.collectAsState()
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()
    val isScheduleLoading by viewModel.isScheduleLoading.collectAsState()

    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    val activeDaySchedule = scheduleList.find { it.day.equals(selectedDay, ignoreCase = true) }
    val animeList = activeDaySchedule?.animeList ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDongBg)
            .testTag("schedule_screen_content")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AnimeDongGold, CircleShape),
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
                        text = "Jadwal Rilis",
                        color = AnimeDongTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jadwal penayangan episode baru per hari",
                        color = AnimeDongTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = { viewModel.loadSchedule() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Muat ulang jadwal",
                    tint = AnimeDongGold
                )
            }
        }

        // Days selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { day ->
                val isSelected = day.equals(selectedDay, ignoreCase = true)
                Surface(
                    color = if (isSelected) AnimeDongGold else AnimeDongCard,
                    shape = RoundedCornerShape(20.dp),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, AnimeDongCardBorder),
                    modifier = Modifier
                        .clickable { viewModel.selectScheduleDay(day) }
                        .testTag("schedule_day_chip_$day")
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) AnimeDongSurface else AnimeDongTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isScheduleLoading && scheduleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AnimeDongGold)
            }
        } else if (animeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "Tidak ada jadwal untuk hari $selectedDay",
                    subtitle = "Episode baru akan muncul otomatis di sini ketika sudah dijadwalkan."
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(animeList, key = { it.id }) { anime ->
                    AnimeHorizontalCard(
                        animeId = anime.id,
                        title = anime.title,
                        poster = anime.poster,
                        subtitle = "${anime.episodes} • Rilis $selectedDay",
                        onClick = { viewModel.openDetail(anime) }
                    )
                }
            }
        }
    }
}
