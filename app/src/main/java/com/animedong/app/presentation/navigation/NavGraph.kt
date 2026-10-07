package com.animedong.app.presentation.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.animedong.app.di.AppContainer
import com.animedong.app.domain.model.ContentType
import com.animedong.app.presentation.detail.DetailScreen
import com.animedong.app.presentation.home.HomeScreen
import com.animedong.app.presentation.library.LibraryScreen
import com.animedong.app.presentation.player.DonghuaPlayerScreen
import com.animedong.app.presentation.player.PlayerScreen
import com.animedong.app.presentation.profile.ProfileScreen
import com.animedong.app.presentation.schedule.ScheduleScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Beranda", Icons.Filled.Home)
    data object Schedule : Screen("schedule", "Jadwal", Icons.Filled.CalendarMonth)
    data object Library : Screen("library", "Koleksi", Icons.Filled.VideoLibrary)
    data object Profile : Screen("profile", "Profil", Icons.Filled.Person)
}

@Composable
fun AnimeDongNavGraph(
    container: AppContainer,
    startEpisodeId: String? = null
) {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Home, Screen.Schedule, Screen.Library, Screen.Profile)
    val activity = LocalContext.current as Activity
    val isPremium by container.billingRepository.isPremium.collectAsState()
    val showAds = !isPremium && container.remoteConfig.adsEnabled

    // Deep link dari notifikasi FCM: langsung buka episode.
    // Notifikasi episode baru selama ini hanya untuk anime.
    LaunchedEffect(startEpisodeId) {
        startEpisodeId?.let {
            navController.navigate("player/${ContentType.ANIME.key}/$it")
        }
    }

    /** Klik episode = jeda natural: cek interstitial dulu (maks 1x/30 mnt). */
    fun openEpisode(type: ContentType, episodeId: String) {
        container.adManager.maybeShowInterstitial(activity) {
            navController.navigate("player/${type.key}/$episodeId")
        }
    }

    fun openDetail(type: ContentType, contentId: String) {
        navController.navigate("detail/${type.key}/$contentId")
    }

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val current = backStack?.destination
            // Sembunyikan bottom bar di layar detail & player
            val showBar = tabs.any { it.route == current?.route }
            if (showBar) {
                AnimeDongBottomBar(
                    tabs = tabs,
                    current = current,
                    onTabClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = container.repository,
                    donghuaRepository = container.donghuaRepository,
                    adManager = container.adManager,
                    showAds = showAds,
                    onContentClick = { type, id -> openDetail(type, id) },
                    onEpisodeClick = { type, id -> openEpisode(type, id) },
                    onSeeAllNew = { navController.navigate(Screen.Schedule.route) }
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    repository = container.repository,
                    donghuaRepository = container.donghuaRepository,
                    onContentClick = { type, id -> openDetail(type, id) }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    repository = container.repository,
                    onContentClick = { type, id -> openDetail(type, id) },
                    onEpisodeClick = { type, id -> openEpisode(type, id) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    container.authManager,
                    container.billingRepository,
                    container.prefs
                )
            }
            composable("detail/{type}/{contentId}") { backStack ->
                val type = ContentType.fromKey(
                    backStack.arguments?.getString("type")
                )
                val id = backStack.arguments?.getString("contentId")
                    ?: return@composable
                DetailScreen(
                    repository = container.repository,
                    donghuaRepository = container.donghuaRepository,
                    adManager = container.adManager,
                    showAds = showAds,
                    contentType = type,
                    contentId = id,
                    onEpisodeClick = { t, epId -> openEpisode(t, epId) }
                )
            }
            composable("player/{type}/{episodeId}") { backStack ->
                val type = ContentType.fromKey(
                    backStack.arguments?.getString("type")
                )
                val id = backStack.arguments?.getString("episodeId")
                    ?: return@composable
                when (type) {
                    ContentType.ANIME -> PlayerScreen(
                        repository = container.repository,
                        episodeId = id,
                        onNavigateEpisode = { epId ->
                            navController.navigate("player/anime/$epId") {
                                popUpTo("player/{type}/{episodeId}") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                    ContentType.DONGHUA -> DonghuaPlayerScreen(
                        donghuaRepository = container.donghuaRepository,
                        animeRepository = container.repository,
                        episodeSlug = id
                    )
                }
            }
        }
    }
}

/**
 * Bottom nav ala desain referensi: tab aktif = pill kuning,
 * tab lain = ikon + label abu-abu.
 */
@Composable
private fun AnimeDongBottomBar(
    tabs: List<Screen>,
    current: androidx.navigation.NavDestination?,
    onTabClick: (Screen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { screen ->
            val selected = current?.hierarchy?.any {
                it.route == screen.route
            } == true
            if (selected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onTabClick(screen) }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        screen.label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabClick(screen) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        screen.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
