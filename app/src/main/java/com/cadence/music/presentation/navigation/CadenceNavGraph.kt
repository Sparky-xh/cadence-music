package com.cadence.music.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cadence.music.presentation.auth.LoginScreen
import com.cadence.music.presentation.auth.SignupScreen
import com.cadence.music.presentation.components.MiniPlayerBar
import com.cadence.music.presentation.creator.CreatorDashboardScreen
import com.cadence.music.presentation.creator.UploadSongScreen
import com.cadence.music.presentation.downloads.DownloadsScreen
import com.cadence.music.presentation.home.HomeScreen
import com.cadence.music.presentation.library.LibraryScreen
import com.cadence.music.presentation.lyrics.LyricsEditorScreen
import com.cadence.music.presentation.lyrics.LyricsScreen
import com.cadence.music.presentation.onboarding.OnboardingScreen
import com.cadence.music.presentation.player.PlayerScreen
import com.cadence.music.presentation.premium.PremiumScreen
import com.cadence.music.presentation.profile.ProfileScreen
import com.cadence.music.presentation.search.SearchScreen
import com.cadence.music.presentation.splash.SplashScreen

private data class NavItem(val destination: Destination, val label: String, val filledIcon: androidx.compose.ui.graphics.vector.ImageVector, val outlineIcon: androidx.compose.ui.graphics.vector.ImageVector)

private val navItems = listOf(
    NavItem(Destination.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Destination.Search, "Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
    NavItem(Destination.Library, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavItem(Destination.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun CadenceNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = navItems.any { it.destination.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showChrome) {
                Column {
                    MiniPlayerBar(onExpand = { navController.navigate(Destination.Player.route) })
                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.destination.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(if (selected) item.filledIcon else item.outlineIcon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Splash.route,
            modifier = Modifier.padding(bottom = if (showChrome) padding.calculateBottomPadding() else 0.dp)
        ) {
            composable(Destination.Splash.route) {
                SplashScreen(
                    onNavigateToLogin = { navController.navigate(Destination.Login.route) { popUpTo(0) } },
                    onNavigateToOnboarding = { navController.navigate(Destination.Onboarding.route) { popUpTo(0) } },
                    onNavigateToHome = { navController.navigate(Destination.Home.route) { popUpTo(0) } }
                )
            }
            composable(Destination.Login.route) {
                LoginScreen(
                    onLoggedIn = { needsOnboarding ->
                        val dest = if (needsOnboarding) Destination.Onboarding else Destination.Home
                        navController.navigate(dest.route) { popUpTo(0) }
                    },
                    onNavigateToSignup = { navController.navigate(Destination.Signup.route) }
                )
            }
            composable(Destination.Signup.route) {
                SignupScreen(
                    onSignedUp = { navController.navigate(Destination.Onboarding.route) { popUpTo(0) } },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Destination.Onboarding.route) {
                OnboardingScreen(onFinished = { navController.navigate(Destination.Home.route) { popUpTo(0) } })
            }

            composable(Destination.Home.route) {
                HomeScreen(
                    onSongClick = { navController.navigate(Destination.Player.route) },
                    onSeeDownloads = { navController.navigate(Destination.Downloads.route) },
                    onSeePremium = { navController.navigate(Destination.Premium.route) }
                )
            }
            composable(Destination.Search.route) {
                SearchScreen(onSongClick = { navController.navigate(Destination.Player.route) })
            }
            composable(Destination.Library.route) {
                LibraryScreen(
                    onSongClick = { navController.navigate(Destination.Player.route) },
                    onSeeDownloads = { navController.navigate(Destination.Downloads.route) }
                )
            }
            composable(Destination.Profile.route) {
                ProfileScreen(
                    onSeePremium = { navController.navigate(Destination.Premium.route) },
                    onSeeCreatorDashboard = { navController.navigate(Destination.CreatorDashboard.route) },
                    onSignedOut = { navController.navigate(Destination.Login.route) { popUpTo(0) } }
                )
            }

            composable(Destination.Player.route) {
                PlayerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLyrics = { navController.navigate(Destination.Lyrics.route) }
                )
            }
            composable(Destination.Lyrics.route) {
                LyricsScreen(
                    onBack = { navController.popBackStack() },
                    onEditLyrics = { navController.navigate(Destination.LyricsEditor.route) }
                )
            }
            composable(Destination.LyricsEditor.route) {
                LyricsEditorScreen(onDone = { navController.popBackStack() })
            }

            composable(Destination.Downloads.route) {
                DownloadsScreen(onBack = { navController.popBackStack() }, onSongClick = { navController.navigate(Destination.Player.route) })
            }
            composable(Destination.Premium.route) {
                PremiumScreen(onBack = { navController.popBackStack() }, onUpgraded = { navController.popBackStack() })
            }

            composable(Destination.CreatorDashboard.route) {
                CreatorDashboardScreen(
                    onBack = { navController.popBackStack() },
                    onUploadNew = { navController.navigate(Destination.UploadSong.route) }
                )
            }
            composable(Destination.UploadSong.route) {
                UploadSongScreen(onUploaded = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }
        }
    }
}
