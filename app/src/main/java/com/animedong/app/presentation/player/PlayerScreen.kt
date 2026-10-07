package com.animedong.app.presentation.player

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.domain.model.EpisodeDetail
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data class Error(val message: String) : PlayerUiState
    data class Ready(val detail: EpisodeDetail) : PlayerUiState
}

class PlayerViewModel(
    private val repo: AnimeRepository,
    private val episodeId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // Dipakai PlayerScreen untuk set media item.
    // Catatan: load() dipanggil dari DisposableEffect PlayerScreen
    // SETELAH onStreamUrl di-set, supaya URL langsung di-play.
    var onStreamUrl: ((String) -> Unit)? = null

    fun load() = viewModelScope.launch {
        _uiState.value = PlayerUiState.Loading
        try {
            val detail = repo.getEpisodeDetail(episodeId)
            _uiState.value = PlayerUiState.Ready(detail)
            detail.defaultStreamingUrl?.let { onStreamUrl?.invoke(it) }
        } catch (e: Exception) {
            _uiState.value = PlayerUiState.Error(e.message ?: "Gagal memuat video")
        }
    }

    /** User pilih server manual -> resolve -> play URL final. */
    fun playServer(serverId: String, onError: (String) -> Unit) =
        viewModelScope.launch {
            try {
                val url = repo.resolveServerUrl(serverId)
                if (url != null) onStreamUrl?.invoke(url)
                else onError("Server tidak merespons, coba yang lain")
            } catch (e: Exception) {
                onError(e.message ?: "Gagal resolve server")
            }
        }

    fun saveProgress(title: String, positionMs: Long) = viewModelScope.launch {
        val current = _uiState.value
        if (current is PlayerUiState.Ready) {
            repo.saveProgress(current.detail.animeId, episodeId, title, positionMs)
        }
    }
}

@OptIn(UnstableApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun PlayerScreen(
    repository: AnimeRepository,
    episodeId: String,
    onNavigateEpisode: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: PlayerViewModel = viewModel { PlayerViewModel(repository, episodeId) }
    val state by vm.uiState.collectAsState()
    var showServers by remember { mutableStateOf(false) }
    var snack by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ExoPlayer lifecycle: dibuat sekali, release saat composable hilang
    val player = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }
    DisposableEffect(Unit) {
        vm.onStreamUrl = { url ->
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
        }
        // load ulang karena onStreamUrl baru di-set setelah init
        vm.load()
        onDispose {
            vm.saveProgress(
                (vm.uiState.value as? PlayerUiState.Ready)?.detail?.title ?: "",
                player.currentPosition
            )
            player.release()
        }
    }

    LaunchedEffect(snack) {
        snack?.let {
            snackbarHostState.showSnackbar(it)
            snack = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = {
                Text(
                    (state as? PlayerUiState.Ready)?.detail?.title ?: "Player",
                    maxLines = 1
                )
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            )

            when (val s = state) {
                is PlayerUiState.Loading ->
                    Box(Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                is PlayerUiState.Error ->
                    Box(Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.message)
                            TextButton(onClick = { vm.load() }) { Text("Coba lagi") }
                        }
                    }
                is PlayerUiState.Ready -> {
                    val d = s.detail
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { d.prevEpisodeId?.let(onNavigateEpisode) },
                            enabled = d.prevEpisodeId != null
                        ) { Text("⏮ Prev") }
                        Button(
                            onClick = { d.nextEpisodeId?.let(onNavigateEpisode) },
                            enabled = d.nextEpisodeId != null
                        ) { Text("Next ⏭") }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showServers = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) { Text("🔀 Ganti Server") }
                }
            }
        }

        if (showServers && state is PlayerUiState.Ready) {
            val detail = (state as PlayerUiState.Ready).detail
            ModalBottomSheet(onDismissRequest = { showServers = false }) {
                Column(Modifier.padding(16.dp)) {
                    Text("Pilih Server", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    detail.qualities.forEach { quality ->
                        Text(
                            quality.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        quality.servers.forEach { server ->
                            ListItem(
                                headlineContent = { Text(server.title) },
                                supportingContent = { Text(quality.title) },
                                modifier = Modifier.clickable {
                                    showServers = false
                                    vm.playServer(server.serverId) { msg -> snack = msg }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // snackbar sudah di-handle LaunchedEffect di atas
    }
}
