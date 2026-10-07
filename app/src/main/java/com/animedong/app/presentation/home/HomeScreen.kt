package com.animedong.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animedong.app.core.ui.SectionHeader
import com.animedong.app.data.ads.AdManager
import com.animedong.app.data.ads.BannerAd
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.data.repository.DonghuaRepository
import com.animedong.app.domain.model.AnimeSummary
import com.animedong.app.domain.model.ContentType
import com.animedong.app.domain.model.DonghuaHome
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(
        val ongoing: List<AnimeSummary>,
        val completed: List<AnimeSummary>
    ) : HomeUiState
}

sealed interface DonghuaHomeUiState {
    data object Loading : DonghuaHomeUiState
    data class Error(val message: String) : DonghuaHomeUiState
    data class Success(val home: DonghuaHome) : DonghuaHomeUiState
}

class HomeViewModel(
    private val repo: AnimeRepository,
    private val donghuaRepo: DonghuaRepository
) : ViewModel() {
    private val _animeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val animeState: StateFlow<HomeUiState> = _animeState.asStateFlow()

    private val _donghuaState =
        MutableStateFlow<DonghuaHomeUiState>(DonghuaHomeUiState.Loading)
    val donghuaState: StateFlow<DonghuaHomeUiState> = _donghuaState.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _animeState.value = HomeUiState.Loading
        _donghuaState.value = DonghuaHomeUiState.Loading
        // Dua backend independen — load paralel, gagal satu tidak mematikan satunya.
        val animeJob = async {
            runCatching {
                val (ongoing, completed) = repo.getHome()
                HomeUiState.Success(ongoing, completed)
            }.getOrElse { HomeUiState.Error(it.message ?: "Gagal memuat anime") }
        }
        val donghuaJob = async {
            runCatching { DonghuaHomeUiState.Success(donghuaRepo.getHome()) }
                .getOrElse {
                    DonghuaHomeUiState.Error(it.message ?: "Gagal memuat donghua")
                }
        }
        _animeState.value = animeJob.await()
        _donghuaState.value = donghuaJob.await()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: AnimeRepository,
    donghuaRepository: DonghuaRepository,
    adManager: AdManager,
    showAds: Boolean,
    onContentClick: (ContentType, String) -> Unit,
    onEpisodeClick: (ContentType, String) -> Unit,
    onSeeAllNew: () -> Unit
) {
    val vm: HomeViewModel = viewModel { HomeViewModel(repository, donghuaRepository) }
    val animeState by vm.animeState.collectAsState()
    val donghuaState by vm.donghuaState.collectAsState()
    var tab by remember { mutableStateOf(ContentType.DONGHUA) }

    Column(Modifier.fillMaxSize()) {
        // Toggle sumber konten — default Donghua sesuai identitas app.
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ContentType.entries.forEachIndexed { i, type ->
                SegmentedButton(
                    selected = tab == type,
                    onClick = { tab = type },
                    shape = SegmentedButtonDefaults.itemShape(i, ContentType.entries.size),
                    label = {
                        Text(if (type == ContentType.ANIME) "Anime" else "Donghua")
                    }
                )
            }
        }
        when (tab) {
            ContentType.ANIME -> AnimeHomeContent(
                state = animeState,
                onRetry = { vm.refresh() },
                repository = repository,
                onContentClick = { onContentClick(ContentType.ANIME, it) },
                onEpisodeClick = { onEpisodeClick(ContentType.ANIME, it) },
                onSeeAllNew = onSeeAllNew,
                adManager = adManager,
                showAds = showAds
            )
            ContentType.DONGHUA -> DonghuaHomeContent(
                state = donghuaState,
                onRetry = { vm.refresh() },
                donghuaRepository = donghuaRepository,
                onContentClick = { onContentClick(ContentType.DONGHUA, it) },
                onEpisodeClick = { onEpisodeClick(ContentType.DONGHUA, it) },
                adManager = adManager,
                showAds = showAds
            )
        }
    }
}

@Composable
private fun AnimeHomeContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    repository: AnimeRepository,
    onContentClick: (String) -> Unit,
    onEpisodeClick: (String) -> Unit,
    onSeeAllNew: () -> Unit,
    adManager: AdManager,
    showAds: Boolean
) {
    val scope = rememberCoroutineScope()
    when (state) {
        is HomeUiState.Loading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        is HomeUiState.Error ->
            ErrorBox(message = state.message, onRetry = onRetry)
        is HomeUiState.Success -> {
            val spotlight = state.ongoing.take(5)
            val ongoing = state.ongoing.take(10)
            val completed = state.completed.take(10)
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    if (spotlight.isNotEmpty()) {
                        SpotlightPager(
                            items = spotlight,
                            onWatch = { anime ->
                                scope.launch {
                                    val detail = runCatching {
                                        repository.getAnimeDetail(anime.id)
                                    }.getOrNull()
                                    val ep = detail?.episodes?.lastOrNull()
                                    if (ep != null) onEpisodeClick(ep.episodeId)
                                    else onContentClick(anime.id)
                                }
                            },
                            onDetail = { onContentClick(it.id) }
                        )
                    }
                }
                item {
                    SectionHeader(
                        title = "Sedang Tayang",
                        actionText = "Jadwal",
                        onAction = onSeeAllNew
                    )
                }
                item {
                    PosterRail(items = ongoing, onClick = onContentClick)
                }
                item { SectionHeader(title = "Sudah Tamat") }
                item {
                    PosterGrid(items = completed, onClick = onContentClick)
                }
                item { BannerAd(showAds, adManager, Modifier.padding(vertical = 8.dp)) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DonghuaHomeContent(
    state: DonghuaHomeUiState,
    onRetry: () -> Unit,
    donghuaRepository: DonghuaRepository,
    onContentClick: (String) -> Unit,
    onEpisodeClick: (String) -> Unit,
    adManager: AdManager,
    showAds: Boolean
) {
    val scope = rememberCoroutineScope()
    when (state) {
        is DonghuaHomeUiState.Loading ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        is DonghuaHomeUiState.Error ->
            ErrorBox(message = state.message, onRetry = onRetry)
        is DonghuaHomeUiState.Success -> {
            val home = state.home
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    if (home.popularToday.isNotEmpty()) {
                        SpotlightPager(
                            items = home.popularToday.take(5),
                            badge = "TERPOPULER",
                            onWatch = { anime ->
                                scope.launch {
                                    val detail = runCatching {
                                        donghuaRepository.getDetail(anime.id)
                                    }.getOrNull()
                                    val ep = detail?.episodes?.lastOrNull()
                                    if (ep != null) onEpisodeClick(ep.episodeId)
                                    else onContentClick(anime.id)
                                }
                            },
                            onDetail = { onContentClick(it.id) }
                        )
                    }
                }
                item { SectionHeader(title = "Rilisan Terbaru") }
                item {
                    PosterRail(items = home.latest.take(20), onClick = onContentClick)
                }
                if (home.movies.isNotEmpty()) {
                    item { SectionHeader(title = "Film Donghua") }
                    item {
                        PosterRail(items = home.movies.take(10), onClick = onContentClick)
                    }
                }
                if (home.recommendations.isNotEmpty()) {
                    item { SectionHeader(title = "Rekomendasi") }
                    item {
                        PosterGrid(
                            items = home.recommendations.take(10),
                            onClick = onContentClick
                        )
                    }
                }
                item { BannerAd(showAds, adManager, Modifier.padding(vertical = 8.dp)) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Gagal memuat: $message")
            TextButton(onClick = onRetry) { Text("Coba lagi") }
        }
    }
}

@Composable
private fun PosterRail(
    items: List<AnimeSummary>,
    onClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { anime ->
            PosterCard(
                anime = anime,
                onClick = { onClick(anime.id) },
                modifier = Modifier.width(130.dp)
            )
        }
    }
}

@Composable
private fun PosterGrid(
    items: List<AnimeSummary>,
    onClick: (String) -> Unit
) {
    // Grid 2 kolom di dalam LazyColumn: Column + rows manual agar tidak nested scroll.
    Column(Modifier.padding(horizontal = 16.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { anime ->
                    PosterCard(
                        anime = anime,
                        onClick = { onClick(anime.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SpotlightPager(
    items: List<AnimeSummary>,
    badge: String = "SPOTLIGHT",
    onWatch: (AnimeSummary) -> Unit,
    onDetail: (AnimeSummary) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
        ) { page ->
            val anime = items[page]
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onDetail(anime) }
            ) {
                AsyncImage(
                    model = anime.poster,
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x990E1218),
                                    Color(0xFF0E1218)
                                )
                            )
                        )
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        anime.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    anime.subtitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onWatch(anime) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Filled.PlayArrow, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Nonton")
                        }
                        OutlinedButton(onClick = { onDetail(anime) }) {
                            Text("Detail")
                        }
                    }
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { i ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(
                            width = if (i == pagerState.currentPage) 20.dp else 7.dp,
                            height = 7.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (i == pagerState.currentPage)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun PosterCard(
    anime: AnimeSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = anime.poster,
            contentDescription = anime.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            anime.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        anime.subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
