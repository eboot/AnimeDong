package com.animedong.app.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animedong.app.core.ui.SectionHeader
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.data.repository.DonghuaRepository
import com.animedong.app.domain.model.AnimeSummary
import com.animedong.app.domain.model.ContentType
import com.animedong.app.domain.model.ScheduleDay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleViewModel(
    private val repo: AnimeRepository,
    private val donghuaRepo: DonghuaRepository
) : ViewModel() {
    private val _anime =
        MutableStateFlow<Result<List<ScheduleDay>>?>(null)
    val anime: StateFlow<Result<List<ScheduleDay>>?> = _anime.asStateFlow()

    private val _donghua =
        MutableStateFlow<Result<List<ScheduleDay>>?>(null)
    val donghua: StateFlow<Result<List<ScheduleDay>>?> = _donghua.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val a = async { runCatching { repo.getSchedule() } }
        val d = async { runCatching { donghuaRepo.getSchedule() } }
        _anime.value = a.await()
        _donghua.value = d.await()
    }
}

private val DAYS = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
private val DAY_SHORT = listOf("SEN", "SEL", "RAB", "KAM", "JUM", "SAB", "MIN")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    repository: AnimeRepository,
    donghuaRepository: DonghuaRepository,
    onContentClick: (ContentType, String) -> Unit
) {
    val vm: ScheduleViewModel = viewModel {
        ScheduleViewModel(repository, donghuaRepository)
    }
    val animeState by vm.anime.collectAsState()
    val donghuaState by vm.donghua.collectAsState()

    val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    var selectedDay by remember { mutableIntStateOf(todayIndex) }
    var tab by remember { mutableStateOf(ContentType.DONGHUA) }

    val state = if (tab == ContentType.ANIME) animeState else donghuaState

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Jadwal Rilis Mingguan",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Episode baru sesuai hari rilisnya",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ContentType.entries.forEachIndexed { i, type ->
                    SegmentedButton(
                        selected = tab == type,
                        onClick = { tab = type },
                        shape = SegmentedButtonDefaults.itemShape(
                            i, ContentType.entries.size
                        ),
                        label = {
                            Text(if (type == ContentType.ANIME) "Anime" else "Donghua")
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(DAYS.size) { i ->
                    val selected = i == selectedDay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedDay = i }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            DAY_SHORT[i],
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            DAYS[i],
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        when (val s = state) {
            null -> item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            else -> s.fold(
                onSuccess = { days ->
                    val day = days.find { it.day.equals(DAYS[selectedDay], ignoreCase = true) }
                    val list = day?.animeList.orEmpty()
                    item {
                        SectionHeader(
                            title = "Jadwal: ${DAYS[selectedDay]} (${list.size} judul)"
                        )
                    }
                    if (list.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada jadwal hari ini",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(list) { anime ->
                            ScheduleCard(
                                anime = anime,
                                onClick = { onContentClick(anime.contentType, anime.id) },
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }
                },
                onFailure = { e ->
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Gagal memuat: ${e.message}")
                                TextButton(onClick = { vm.refresh() }) {
                                    Text("Coba lagi")
                                }
                            }
                        }
                    }
                }
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ScheduleCard(
    anime: AnimeSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Backend tidak menyediakan jam tayang, jadi badge waktu
    // tidak ditampilkan (dilarang mengarang data).
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = anime.poster,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 64.dp, height = 88.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                anime.subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = { /* TODO: reminder FCM per judul */ }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Ingatkan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledIconButton(
                onClick = onClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Putar")
            }
        }
    }
}
