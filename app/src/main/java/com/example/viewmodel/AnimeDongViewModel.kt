package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.GoogleAuthManager
import com.example.data.firebase.RemoteConfigManager
import com.example.data.local.AnimeDongDatabase
import com.example.data.local.UserSessionEntity
import com.example.data.remote.ApiClientProvider
import com.example.data.repository.AnimeRepositoryImpl
import com.example.domain.model.Anime
import com.example.domain.model.AnimeDetail
import com.example.domain.model.Bookmark
import com.example.domain.model.EpisodeInfo
import com.example.domain.model.EpisodeStream
import com.example.domain.model.ScheduleDay
import com.example.domain.model.ServerOption
import com.example.domain.model.WatchHistory
import com.example.domain.repository.AnimeRepository
import com.example.ui.components.PrivateDnsChecker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SCHEDULE,
    LIBRARY,
    PROFILE,
    DETAIL,
    PLAYER
}

class AnimeDongViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AnimeDongDatabase.getDatabase(application)
    val repository: AnimeRepository = AnimeRepositoryImpl(database.animeDongDao())
    val remoteConfigManager = RemoteConfigManager.getInstance(application)
    private val googleAuthManager = GoogleAuthManager(application)

    // User Session State (Persistent via Room)
    val userSession: StateFlow<UserSessionEntity?> = database.animeDongDao()
        .getUserSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Private DNS Blocking State
    private val _isPrivateDnsBlocked = MutableStateFlow(false)
    val isPrivateDnsBlocked: StateFlow<Boolean> = _isPrivateDnsBlocked.asStateFlow()

    // Navigation Stack
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    private val screenBackStack = mutableListOf<Screen>()

    // Remote Config State
    val apiBaseUrl = remoteConfigManager.apiBaseUrl
    val defaultQuality = remoteConfigManager.defaultQuality
    val serverPickerEnabled = remoteConfigManager.serverPickerEnabled
    val adsEnabled = remoteConfigManager.adsEnabled

    // Home Data
    private val _ongoingList = MutableStateFlow<List<Anime>>(emptyList())
    val ongoingList: StateFlow<List<Anime>> = _ongoingList.asStateFlow()

    private val _completedList = MutableStateFlow<List<Anime>>(emptyList())
    val completedList: StateFlow<List<Anime>> = _completedList.asStateFlow()

    private val _isHomeLoading = MutableStateFlow(false)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    // Schedule Data
    private val _scheduleList = MutableStateFlow<List<ScheduleDay>>(emptyList())
    val scheduleList: StateFlow<List<ScheduleDay>> = _scheduleList.asStateFlow()

    private val _selectedScheduleDay = MutableStateFlow("Senin")
    val selectedScheduleDay: StateFlow<String> = _selectedScheduleDay.asStateFlow()

    private val _isScheduleLoading = MutableStateFlow(false)
    val isScheduleLoading: StateFlow<Boolean> = _isScheduleLoading.asStateFlow()

    // Detail & Player State
    private val _selectedAnime = MutableStateFlow<Anime?>(null)
    val selectedAnime: StateFlow<Anime?> = _selectedAnime.asStateFlow()

    private val _selectedAnimeDetail = MutableStateFlow<AnimeDetail?>(null)
    val selectedAnimeDetail: StateFlow<AnimeDetail?> = _selectedAnimeDetail.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    // Player State
    private val _currentEpisode = MutableStateFlow<EpisodeInfo?>(null)
    val currentEpisode: StateFlow<EpisodeInfo?> = _currentEpisode.asStateFlow()

    private val _currentStream = MutableStateFlow<EpisodeStream?>(null)
    val currentStream: StateFlow<EpisodeStream?> = _currentStream.asStateFlow()

    private val _activeStreamUrl = MutableStateFlow<String?>(null)
    val activeStreamUrl: StateFlow<String?> = _activeStreamUrl.asStateFlow()

    private val _selectedServerOption = MutableStateFlow<ServerOption?>(null)
    val selectedServerOption: StateFlow<ServerOption?> = _selectedServerOption.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(1200000L) // 20 mins default
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _isPlayerLoading = MutableStateFlow(false)
    val isPlayerLoading: StateFlow<Boolean> = _isPlayerLoading.asStateFlow()

    // Library Tab State
    private val _libraryTab = MutableStateFlow(0) // 0: Bookmark, 1: Riwayat
    val libraryTab: StateFlow<Int> = _libraryTab.asStateFlow()

    val bookmarks: StateFlow<List<Bookmark>> = repository.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistory>> = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Snackbar Messages
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        // Check Private DNS on launch
        checkPrivateDns()

        // Fetch Remote Config
        remoteConfigManager.fetchAndActivate { success ->
            Log.d("AnimeDongViewModel", "Remote config fetch result: $success")
            val newUrl = remoteConfigManager.apiBaseUrl.value
            ApiClientProvider.getInstance().updateBaseUrl(newUrl)
        }

        // Listen for base URL updates
        viewModelScope.launch {
            remoteConfigManager.apiBaseUrl.collect { newBaseUrl ->
                ApiClientProvider.getInstance().updateBaseUrl(newBaseUrl)
            }
        }

        // Load initial home data
        loadHomeData()
        loadSchedule()
    }

    fun checkPrivateDns() {
        val blocked = PrivateDnsChecker.isPrivateDnsActive(getApplication())
        _isPrivateDnsBlocked.value = blocked
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isHomeLoading.value = true
            repository.getHomeOngoing().collect { result ->
                result.onSuccess { _ongoingList.value = it }
                    .onFailure { _snackbarMessage.emit("Gagal memuat anime ongoing: ${it.localizedMessage}") }
            }
            repository.getHomeCompleted().collect { result ->
                result.onSuccess { _completedList.value = it }
                    .onFailure { Log.e("AnimeDongViewModel", "Gagal memuat completed: ${it.message}") }
            }
            _isHomeLoading.value = false
        }
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _isScheduleLoading.value = true
            repository.getSchedule().collect { result ->
                result.onSuccess {
                    _scheduleList.value = it
                    if (it.isNotEmpty() && _selectedScheduleDay.value.isBlank()) {
                        _selectedScheduleDay.value = it.first().day
                    }
                }
            }
            _isScheduleLoading.value = false
        }
    }

    fun selectScheduleDay(day: String) {
        _selectedScheduleDay.value = day
    }

    fun selectLibraryTab(tab: Int) {
        _libraryTab.value = tab
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openDetail(anime: Anime) {
        _selectedAnime.value = anime
        _selectedAnimeDetail.value = null
        checkBookmarkStatus(anime.id)
        navigateTo(Screen.DETAIL)

        viewModelScope.launch {
            _isDetailLoading.value = true
            repository.getAnimeDetail(anime.id).collect { result ->
                result.onSuccess { detail ->
                    _selectedAnimeDetail.value = detail
                }.onFailure {
                    _snackbarMessage.emit("Gagal memuat episode: ${it.localizedMessage}")
                }
                _isDetailLoading.value = false
            }
        }
    }

    private fun checkBookmarkStatus(animeId: String) {
        viewModelScope.launch {
            repository.isBookmarked(animeId).collect {
                _isBookmarked.value = it
            }
        }
    }

    fun toggleBookmark() {
        val anime = _selectedAnime.value ?: return
        viewModelScope.launch {
            repository.toggleBookmark(anime.id, anime.title, anime.poster)
            val isNowBookmarked = !_isBookmarked.value
            _isBookmarked.value = isNowBookmarked
            _snackbarMessage.emit(
                if (isNowBookmarked) "Ditambahkan ke Bookmark" else "Dihapus dari Bookmark"
            )
        }
    }

    fun playEpisode(episode: EpisodeInfo) {
        val anime = _selectedAnime.value ?: return
        _currentEpisode.value = episode
        _currentStream.value = null
        _activeStreamUrl.value = null
        _selectedServerOption.value = null
        _playbackPositionMs.value = 0L
        _isPlaying.value = true
        navigateTo(Screen.PLAYER)

        viewModelScope.launch {
            _isPlayerLoading.value = true
            repository.getEpisodeStream(episode.episodeId).collect { result ->
                result.onSuccess { stream ->
                    _currentStream.value = stream
                    // Use default streaming URL or first server
                    val initialUrl = stream.defaultUrl
                    if (!initialUrl.isNullOrBlank()) {
                        _activeStreamUrl.value = initialUrl
                    } else {
                        val firstServer = stream.qualities.firstOrNull()?.servers?.firstOrNull()
                        if (firstServer != null) {
                            switchServer(firstServer)
                        }
                    }

                    // Save watch history
                    repository.saveHistory(
                        episodeId = episode.episodeId,
                        animeId = anime.id,
                        title = "${anime.title} - ${episode.title}",
                        positionMs = 0L
                    )
                }.onFailure {
                    _snackbarMessage.emit("Gagal memuat video: ${it.localizedMessage}")
                }
                _isPlayerLoading.value = false
            }
        }
    }

    // Switch server dynamically (never cached, always fetch fresh)
    fun switchServer(serverOption: ServerOption) {
        _selectedServerOption.value = serverOption
        viewModelScope.launch {
            _isPlayerLoading.value = true
            repository.getServerFinalUrl(serverOption.serverId).collect { result ->
                result.onSuccess { finalUrl ->
                    _activeStreamUrl.value = finalUrl
                    _snackbarMessage.emit("Beralih ke server: ${serverOption.title}")
                }.onFailure {
                    _snackbarMessage.emit("Server tidak dapat diputar: ${it.localizedMessage}")
                }
                _isPlayerLoading.value = false
            }
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs.coerceIn(0L, _totalDurationMs.value)
    }

    fun seekRelative(secondsDelta: Int) {
        val nextPos = (_playbackPositionMs.value + (secondsDelta * 1000L))
            .coerceIn(0L, _totalDurationMs.value)
        _playbackPositionMs.value = nextPos
    }

    fun nextEpisode() {
        val nextId = _currentStream.value?.nextEpisodeId
        val detail = _selectedAnimeDetail.value
        if (!nextId.isNullOrBlank() && detail != null) {
            val nextEp = detail.episodes.find { it.episodeId == nextId }
            if (nextEp != null) {
                playEpisode(nextEp)
                return
            }
        }
        viewModelScope.launch {
            _snackbarMessage.emit("Ini adalah episode terakhir!")
        }
    }

    fun previousEpisode() {
        val prevId = _currentStream.value?.prevEpisodeId
        val detail = _selectedAnimeDetail.value
        if (!prevId.isNullOrBlank() && detail != null) {
            val prevEp = detail.episodes.find { it.episodeId == prevId }
            if (prevEp != null) {
                playEpisode(prevEp)
            }
        }
    }

    fun deleteHistoryItem(episodeId: String) {
        viewModelScope.launch {
            repository.deleteHistory(episodeId)
            _snackbarMessage.emit("Riwayat dihapus")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _snackbarMessage.emit("Semua riwayat berhasil dibersihkan")
        }
    }

    // Google Sign-In & Auth
    fun signInWithGoogle(onFallbackNeeded: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            when (val result = googleAuthManager.signInWithGoogle()) {
                is AuthResult.Success -> {
                    val user = result.user
                    val session = UserSessionEntity(
                        email = user.email,
                        displayName = user.displayName,
                        photoUrl = user.photoUrl,
                        idToken = user.idToken,
                        isVip = true
                    )
                    database.animeDongDao().saveUserSession(session)
                    _isLoggingIn.value = false
                    _snackbarMessage.emit("Berhasil masuk! Selamat datang, ${user.displayName}")
                }
                is AuthResult.Cancelled -> {
                    _isLoggingIn.value = false
                    _snackbarMessage.emit("Pemilihan akun Google dibatalkan")
                }
                is AuthResult.Error -> {
                    _isLoggingIn.value = false
                    onFallbackNeeded()
                }
            }
        }
    }

    fun signInDirectly(email: String, displayName: String, photoUrl: String? = null) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            val session = UserSessionEntity(
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                isVip = true
            )
            database.animeDongDao().saveUserSession(session)
            _isLoggingIn.value = false
            _snackbarMessage.emit("Berhasil masuk dengan akun Google: $email")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthManager.signOut()
            database.animeDongDao().clearUserSession()
            _snackbarMessage.emit("Anda telah keluar dari akun Google")
        }
    }

    // Navigation Stack
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val previousScreen = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = previousScreen
            return true
        }
        return false
    }
}
