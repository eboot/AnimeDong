package com.animedong.app.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animedong.app.data.ads.AdManager
import com.animedong.app.data.ads.BannerAd
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.data.repository.DonghuaRepository
import com.animedong.app.domain.model.AnimeDetail
import com.animedong.app.domain.model.ContentType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val message: String) : DetailUiState
    data class Success(val detail: AnimeDetail, val bookmarked: Boolean) : DetailUiState
}

class DetailViewModel(
    private val repo: AnimeRepository,
    private val donghuaRepo: DonghuaRepository,
    private val contentType: ContentType,
    private val contentId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.value = DetailUiState.Loading
        try {
            val detail = when (contentType) {
                ContentType.ANIME -> repo.getAnimeDetail(contentId)
                ContentType.DONGHUA -> donghuaRepo.getDetail(contentId)
            }
            _uiState.value = DetailUiState.Success(
                detail, repo.isBookmarked(contentId, contentType)
            )
        } catch (e: Exception) {
            _uiState.value = DetailUiState.Error(e.message ?: "Gagal memuat")
        }
    }

    fun toggleBookmark(detail: AnimeDetail) = viewModelScope.launch {
        repo.toggleBookmark(contentId, detail.title, detail.poster, contentType)
        val current = _uiState.value
        if (current is DetailUiState.Success) {
            _uiState.value =
                current.copy(bookmarked = repo.isBookmarked(contentId, contentType))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    repository: AnimeRepository,
    donghuaRepository: DonghuaRepository,
    adManager: AdManager,
    showAds: Boolean,
    contentType: ContentType,
    contentId: String,
    onEpisodeClick: (ContentType, String) -> Unit
) {
    val vm: DetailViewModel = viewModel {
        DetailViewModel(repository, donghuaRepository, contentType, contentId)
    }
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detail") }) }
    ) { padding ->
        when (val s = state) {
            is DetailUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            is DetailUiState.Error ->
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    Text("Gagal memuat: ${s.message}")
                }
            is DetailUiState.Success -> {
                val d = s.detail
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    item {
                        AsyncImage(
                            model = d.poster,
                            contentDescription = d.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                        Column(Modifier.padding(16.dp)) {
                            Text(d.title, style = MaterialTheme.typography.headlineSmall)
                            d.japanese?.let {
                                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOfNotNull(d.score?.let { "⭐ $it" }, d.status, d.type, d.studios)
                                    .forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                d.genres.forEach {
                                    AssistChip(onClick = {}, label = { Text(it) })
                                }
                            }
                            d.synopsis?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(12.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Episode (${d.episodes.size})",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                FilledTonalButton(
                                    onClick = { vm.toggleBookmark(d) }
                                ) {
                                    Text(if (s.bookmarked) "★ Bookmark" else "☆ Bookmark")
                                }
                            }
                        }
                    }
                    items(d.episodes) { ep ->
                        ListItem(
                            headlineContent = {
                                Text(ep.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = { ep.date?.let { Text(it) } },
                            leadingContent = {
                                FilledTonalIconButton(onClick = {}) {
                                    Text("${ep.number ?: "?"}")
                                }
                            },
                            trailingContent = { Text("▶") },
                            modifier = Modifier.clickable {
                                onEpisodeClick(ep.contentType, ep.episodeId)
                            }
                        )
                    }
                    item { BannerAd(showAds, adManager, Modifier.padding(vertical = 8.dp)) }
                }
            }
        }
    }
}
