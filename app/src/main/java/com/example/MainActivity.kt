package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrivateDnsBlockedDialog
import com.example.ui.components.PrivateDnsChecker
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.AnimeDongBg
import com.example.ui.theme.AnimeDongCardBorder
import com.example.ui.theme.AnimeDongGold
import com.example.ui.theme.AnimeDongSurface
import com.example.ui.theme.AnimeDongTextMuted
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AnimeDongViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

data class NavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeDongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AnimeDongApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check Private DNS every time app returns to foreground
        viewModel.checkPrivateDns()
    }
}

@Composable
fun AnimeDongApp(viewModel: AnimeDongViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isPrivateDnsBlocked by viewModel.isPrivateDnsBlocked.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navItems = listOf(
        NavItem(Screen.HOME, "Beranda", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavItem(Screen.SCHEDULE, "Jadwal", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_schedule"),
        NavItem(Screen.LIBRARY, "Koleksi", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary, "nav_library"),
        NavItem(Screen.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
    )

    // Hardware back handler
    BackHandler(enabled = currentScreen != Screen.HOME) {
        if (!viewModel.navigateBack()) {
            viewModel.navigateTo(Screen.HOME)
        }
    }

    // Hide bottom navigation bar in detail and video player screens
    val isBottomBarVisible = currentScreen in listOf(
        Screen.HOME,
        Screen.SCHEDULE,
        Screen.LIBRARY,
        Screen.PROFILE
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeDongBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = AnimeDongSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(
                            width = 0.8.dp,
                            color = AnimeDongCardBorder
                        )
                        .testTag("main_bottom_nav")
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AnimeDongBg,
                                unselectedIconColor = AnimeDongTextMuted,
                                selectedTextColor = AnimeDongGold,
                                unselectedTextColor = AnimeDongTextMuted,
                                indicatorColor = AnimeDongGold
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(viewModel = viewModel)
                Screen.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                Screen.LIBRARY -> LibraryScreen(viewModel = viewModel)
                Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                Screen.DETAIL -> DetailScreen(viewModel = viewModel)
                Screen.PLAYER -> PlayerScreen(viewModel = viewModel)
            }

            // Private DNS Blocking Dialog
            if (isPrivateDnsBlocked) {
                PrivateDnsBlockedDialog(
                    onOpenSettings = {
                        PrivateDnsChecker.openDnsSettings(context)
                    },
                    onRestart = {
                        PrivateDnsChecker.restartApp(context)
                    }
                )
            }
        }
    }
}
