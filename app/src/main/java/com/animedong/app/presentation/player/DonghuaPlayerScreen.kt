package com.animedong.app.presentation.player

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animedong.app.data.dto.DonghuaSlugs
import com.animedong.app.data.repository.AnimeRepository
import com.animedong.app.data.repository.DonghuaRepository
import com.animedong.app.domain.model.ContentType
import com.animedong.app.domain.model.DonghuaStream
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DonghuaPlayerUiState {
    data object Loading : DonghuaPlayerUiState
    data class Error(val message: String) : DonghuaPlayerUiState
    data class Ready(val stream: DonghuaStream, val serverIndex: Int) :
        DonghuaPlayerUiState
}

class DonghuaPlayerViewModel(
    private val repo: DonghuaRepository,
    private val episodeSlug: String
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<DonghuaPlayerUiState>(DonghuaPlayerUiState.Loading)
    val uiState: StateFlow<DonghuaPlayerUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.value = DonghuaPlayerUiState.Loading
        try {
            val stream = repo.getStream(episodeSlug)
            _uiState.value = DonghuaPlayerUiState.Ready(stream, 0)
        } catch (e: Exception) {
            _uiState.value = DonghuaPlayerUiState.Error(e.message ?: "Gagal memuat video")
        }
    }

    fun selectServer(index: Int) {
        val current = _uiState.value
        if (current is DonghuaPlayerUiState.Ready) {
            _uiState.value = current.copy(serverIndex = index)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DonghuaPlayerScreen(
    donghuaRepository: DonghuaRepository,
    animeRepository: AnimeRepository,
    episodeSlug: String
) {
    val vm: DonghuaPlayerViewModel = viewModel {
        DonghuaPlayerViewModel(donghuaRepository, episodeSlug)
    }
    val state by vm.uiState.collectAsState()
    var showServers by remember { mutableStateOf(false) }

    // Catat ke riwayat saat stream siap (tanpa posisi — player embed
    // tidak bisa resume, jadi yang tercatat hanya "sudah ditonton").
    LaunchedEffect(state) {
        val s = state
        if (s is DonghuaPlayerUiState.Ready) {
            animeRepository.saveProgress(
                animeId = DonghuaSlugs.seriesSlugFromCard(episodeSlug),
                episodeId = episodeSlug,
                title = s.stream.title,
                positionMs = 0L,
                contentType = ContentType.DONGHUA
            )
        }
    }

    val ready = state as? DonghuaPlayerUiState.Ready
    val currentUrl = ready?.stream?.servers
        ?.getOrNull(ready.serverIndex)?.url

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    ready?.stream?.title ?: "Player Donghua",
                    maxLines = 1
                )
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Embed page (ok.ru, abyssplayer, dsb) — bukan file video,
            // jadi dibuka pakai WebView, bukan ExoPlayer.
            if (currentUrl != null) {
                key(currentUrl) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webViewClient = WebViewClient()
                                loadUrl(currentUrl)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                    )
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentAlignment = Alignment.Center
                ) {
                    when (state) {
                        is DonghuaPlayerUiState.Loading -> CircularProgressIndicator()
                        is DonghuaPlayerUiState.Error ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text((state as DonghuaPlayerUiState.Error).message)
                                TextButton(onClick = { vm.load() }) { Text("Coba lagi") }
                            }
                        else -> Unit
                    }
                }
            }

            if (ready != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showServers = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    val name = ready.stream.servers
                        .getOrNull(ready.serverIndex)?.name
                    Text("Server: ${name ?: "-"} — ganti")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Server diambil langsung dari API. " +
                        "Kalau satu server mati, coba server lain.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (showServers && ready != null) {
            ModalBottomSheet(onDismissRequest = { showServers = false }) {
                Column(Modifier.padding(16.dp)) {
                    Text("Pilih Server", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    ready.stream.servers.forEachIndexed { i, server ->
                        ListItem(
                            headlineContent = { Text(server.name) },
                            trailingContent = {
                                if (i == ready.serverIndex) Text("✓")
                            },
                            modifier = Modifier.clickable {
                                showServers = false
                                vm.selectServer(i)
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
