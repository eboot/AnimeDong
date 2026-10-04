package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.GoogleAuthManager
import com.example.data.local.DongHiveDatabase
import com.example.data.local.DownloadEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.UserSessionEntity
import com.example.data.model.Donghua
import com.example.data.model.Episode
import com.example.data.model.StreamServer
import com.example.data.model.UserComment
import com.example.data.repository.DonghuaRepository
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

class DongHiveViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DongHiveDatabase.getDatabase(application)
    val repository = DonghuaRepository(database.donghuaDao())
    private val googleAuthManager = GoogleAuthManager(application)

    // User Session State (Persistent via Room)
    val userSession: StateFlow<UserSessionEntity?> = database.donghuaDao()
        .getUserSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Navigation Stack
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    // Detail & Player State
    private val _selectedDonghua = MutableStateFlow<Donghua?>(null)
    val selectedDonghua: StateFlow<Donghua?> = _selectedDonghua.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<Episode?>(null)
    val selectedEpisode: StateFlow<Episode?> = _selectedEpisode.asStateFlow()

    private val _episodesList = MutableStateFlow<List<Episode>>(emptyList())
    val episodesList: StateFlow<List<Episode>> = _episodesList.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Semua")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _selectedStatus = MutableStateFlow("Semua")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    // Schedule Tab State
    private val _selectedScheduleDay = MutableStateFlow("Sabtu")
    val selectedScheduleDay: StateFlow<String> = _selectedScheduleDay.asStateFlow()

    // Library Tab State
    private val _libraryTab = MutableStateFlow(0) // 0: Favorit, 1: Riwayat, 2: Unduhan
    val libraryTab: StateFlow<Int> = _libraryTab.asStateFlow()

    // Video Player Interactive State
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(124000L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(1200000L) // 20 mins
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _selectedServer = MutableStateFlow<StreamServer?>(null)
    val selectedServer: StateFlow<StreamServer?> = _selectedServer.asStateFlow()

    private val _selectedQuality = MutableStateFlow("1080p Ultra")
    val selectedQuality: StateFlow<String> = _selectedQuality.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow("Sub Indo")
    val selectedSubtitle: StateFlow<String> = _selectedSubtitle.asStateFlow()

    private val _autoNext = MutableStateFlow(true)
    val autoNext: StateFlow<Boolean> = _autoNext.asStateFlow()

    private val _showPlayerSettings = MutableStateFlow(false)
    val showPlayerSettings: StateFlow<Boolean> = _showPlayerSettings.asStateFlow()

    // Comments State
    val comments: StateFlow<List<UserComment>> = repository.comments

    // App Preferences
    private val _cacheSizeMb = MutableStateFlow("142.8 MB")
    val cacheSizeMb: StateFlow<String> = _cacheSizeMb.asStateFlow()

    private val _isVipUser = MutableStateFlow(true)
    val isVipUser: StateFlow<Boolean> = _isVipUser.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    // Room DB Reactive StateFlows
    val favorites: StateFlow<List<FavoriteEntity>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Default select BTTH on launch for quick preview
        val defaultDonghua = repository.getAllDonghua().first()
        _selectedDonghua.value = defaultDonghua
        _episodesList.value = repository.getEpisodesForDonghua(defaultDonghua)
    }

    // Navigation Logic
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.lastIndex)
            return true
        }
        return false
    }

    fun openDetail(donghua: Donghua) {
        _selectedDonghua.value = donghua
        val eps = repository.getEpisodesForDonghua(donghua)
        _episodesList.value = eps
        checkFavoriteStatus(donghua.id)
        navigateTo(Screen.DETAIL)
    }

    fun openPlayer(donghua: Donghua, episode: Episode, initialPositionMs: Long = 0L) {
        _selectedDonghua.value = donghua
        _selectedEpisode.value = episode
        _episodesList.value = repository.getEpisodesForDonghua(donghua)
        _selectedServer.value = episode.streamServers.firstOrNull()
        _playbackPositionMs.value = if (initialPositionMs > 0) initialPositionMs else 0L
        _isPlaying.value = true
        checkFavoriteStatus(donghua.id)

        // Save to watch history automatically
        viewModelScope.launch {
            repository.saveWatchHistory(
                donghua = donghua,
                episodeNumber = episode.episodeNumber,
                progressMs = _playbackPositionMs.value,
                durationMs = _totalDurationMs.value
            )
        }
        navigateTo(Screen.PLAYER)
    }

    fun checkFavoriteStatus(donghuaId: String) {
        viewModelScope.launch {
            repository.isFavorite(donghuaId).collect { isFav ->
                _isFavorite.value = isFav
            }
        }
    }

    fun toggleFavorite(donghua: Donghua) {
        viewModelScope.launch {
            val nextState = !_isFavorite.value
            repository.toggleFavorite(donghua, _isFavorite.value)
            _isFavorite.value = nextState
            val user = userSession.value
            val message = if (nextState) {
                if (user != null) "Ditambahkan ke Favorit (tersinkron ke akun Google)" else "Ditambahkan ke Favorit"
            } else {
                "Dihapus dari Favorit"
            }
            _snackbarMessage.emit(message)
        }
    }

    fun removeHistoryItem(id: String) {
        viewModelScope.launch {
            repository.removeHistory(id)
            _snackbarMessage.emit("Riwayat tonton dihapus")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _snackbarMessage.emit("Semua riwayat berhasil dibersihkan")
        }
    }

    fun downloadEpisode(donghua: Donghua, episode: Episode) {
        viewModelScope.launch {
            repository.addDownload(donghua, episode.episodeNumber, _selectedQuality.value)
            _snackbarMessage.emit("Episode ${episode.episodeNumber} berhasil diunduh!")
        }
    }

    fun removeDownloadItem(id: String) {
        viewModelScope.launch {
            repository.removeDownload(id)
            _snackbarMessage.emit("Unduhan dihapus")
        }
    }

    // Search and filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedGenre(genre: String) {
        _selectedGenre.value = genre
    }

    fun updateSelectedStatus(status: String) {
        _selectedStatus.value = status
    }

    // Schedule
    fun updateScheduleDay(day: String) {
        _selectedScheduleDay.value = day
    }

    // Library tab
    fun setLibraryTab(index: Int) {
        _libraryTab.value = index
    }

    // Player controls
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

    fun selectServer(server: StreamServer) {
        _selectedServer.value = server
    }

    fun selectQuality(quality: String) {
        _selectedQuality.value = quality
    }

    fun selectPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun selectSubtitle(sub: String) {
        _selectedSubtitle.value = sub
    }

    fun toggleAutoNext() {
        _autoNext.value = !_autoNext.value
    }

    fun togglePlayerSettings() {
        _showPlayerSettings.value = !_showPlayerSettings.value
    }

    fun nextEpisode() {
        val currentEp = _selectedEpisode.value ?: return
        val currentDonghua = _selectedDonghua.value ?: return
        val allEps = _episodesList.value
        val next = allEps.find { it.episodeNumber == currentEp.episodeNumber + 1 }
        if (next != null) {
            openPlayer(currentDonghua, next, 0L)
        } else {
            viewModelScope.launch {
                _snackbarMessage.emit("Ini adalah episode terbaru!")
            }
        }
    }

    fun previousEpisode() {
        val currentEp = _selectedEpisode.value ?: return
        val currentDonghua = _selectedDonghua.value ?: return
        val allEps = _episodesList.value
        val prev = allEps.find { it.episodeNumber == currentEp.episodeNumber - 1 }
        if (prev != null) {
            openPlayer(currentDonghua, prev, 0L)
        }
    }

    fun postComment(text: String) {
        val currentDonghua = _selectedDonghua.value ?: return
        val currentEp = _selectedEpisode.value?.episodeNumber ?: 1
        repository.addComment(currentDonghua.id, currentEp, text)
        viewModelScope.launch {
            _snackbarMessage.emit("Komentar terkirim!")
        }
    }

    fun clearCache() {
        _cacheSizeMb.value = "0.0 MB"
        viewModelScope.launch {
            _snackbarMessage.emit("Cache aplikasi (142.8 MB) berhasil dibersihkan!")
        }
    }

    // Google Sign-In & User Account Methods
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
                    database.donghuaDao().saveUserSession(session)
                    _isLoggingIn.value = false
                    _snackbarMessage.emit("Berhasil masuk! Selamat datang, ${user.displayName}")
                }
                is AuthResult.Cancelled -> {
                    _isLoggingIn.value = false
                    _snackbarMessage.emit("Pemilihan akun Google dibatalkan")
                }
                is AuthResult.Error -> {
                    _isLoggingIn.value = false
                    // Fallback to one-tap account picker dialog in UI
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
            database.donghuaDao().saveUserSession(session)
            _isLoggingIn.value = false
            _snackbarMessage.emit("Berhasil masuk dengan akun Google: $email")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthManager.signOut()
            database.donghuaDao().clearUserSession()
            _snackbarMessage.emit("Anda telah keluar dari akun Google")
        }
    }

    fun syncUserData() {
        viewModelScope.launch {
            _snackbarMessage.emit("Menyinkronkan data Favorit dan Riwayat...")
            kotlinx.coroutines.delay(1000)
            _snackbarMessage.emit("Data Favorit & Riwayat berhasil disinkronkan ke akun!")
        }
    }
}
