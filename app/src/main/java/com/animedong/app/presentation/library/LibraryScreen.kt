package com.animedong.app.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animedong.app.core.ui.AnimeCard
import com.animedong.app.data.local.BookmarkEntity
import com.animedong.app.data.local.WatchHistoryEntity
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.domain.model.ContentType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(private val repo: AnimeRepository) : ViewModel() {
    private val _history = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())
    val history: StateFlow<List<WatchHistoryEntity>> = _history.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkEntity>> = _bookmarks.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _history.value = repo.getHistory()
        _bookmarks.value = repo.getBookmarks()
    }
}

@Composable
fun LibraryScreen(
    repository: AnimeRepository,
    onContentClick: (ContentType, String) -> Unit,
    onEpisodeClick: (ContentType, String) -> Unit
) {
    val vm: LibraryViewModel = viewModel { LibraryViewModel(repository) }
    val history by vm.history.collectAsState()
    val bookmarks by vm.bookmarks.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    // Refresh tiap kali layar dibuka
    LaunchedEffect(Unit) { vm.refresh() }

    val tabs = listOf(
        "Favorit (${bookmarks.size})",
        "Riwayat (${history.size})",
        "Unduhan (0)"
    )

    Column(Modifier.fillMaxSize()) {
        // Header
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
            Text("Koleksi Saya", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Daftar tontonan, riwayat, dan video offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[tab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = {
                        Text(
                            title,
                            color = if (tab == i) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
        when (tab) {
            0 -> if (bookmarks.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Bookmark,
                    title = "Belum Ada Donghua Favorit",
                    hint = "Tekan tombol bookmark pada donghua yang kamu sukai untuk menyimpannya di sini."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bookmarks.size) { i ->
                        val b = bookmarks[i]
                        AnimeCard(
                            title = b.title,
                            poster = b.poster,
                            onClick = {
                                onContentClick(
                                    ContentType.fromKey(b.contentType), b.animeId
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            1 -> if (history.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "Belum Ada Riwayat",
                    hint = "Episode yang kamu tonton akan tercatat di sini."
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(history) { h ->
                        val type = ContentType.fromKey(h.contentType)
                        ListItem(
                            headlineContent = { Text(h.title, maxLines = 1) },
                            supportingContent = {
                                // Player embed donghua tidak menyimpan posisi —
                                // hanya tampilkan timestamp kalau memang ada.
                                if (h.positionMs > 0) {
                                    Text("Terakhir di ${formatMs(h.positionMs)}")
                                } else {
                                    Text(
                                        if (type == ContentType.DONGHUA) "Donghua"
                                        else "Anime"
                                    )
                                }
                            },
                            trailingContent = { Text("▶") },
                            modifier = Modifier.clickable {
                                onEpisodeClick(type, h.episodeId)
                            }
                        )
                    }
                }
            }
            else -> EmptyState(
                icon = Icons.Filled.Download,
                title = "Belum Ada Unduhan",
                hint = "Fitur unduh video offline segera hadir."
            )
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, hint: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}
