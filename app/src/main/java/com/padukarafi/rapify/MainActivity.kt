package com.padukarafi.rapify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.padukarafi.rapify.ui.components.AmbientBackdrop
import com.padukarafi.rapify.ui.components.GlassNavBar
import com.padukarafi.rapify.ui.components.LocalCoverTint
import com.padukarafi.rapify.ui.components.rememberCoverTint
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.padukarafi.rapify.data.update.UpdateInfo
import com.padukarafi.rapify.data.update.UpdateManager
import com.padukarafi.rapify.ui.components.UpdateDialog
import com.padukarafi.rapify.data.model.Track
import com.padukarafi.rapify.ui.components.FullPlayerSheet
import com.padukarafi.rapify.ui.components.MiniPlayer
import com.padukarafi.rapify.ui.navigation.Screen
import com.padukarafi.rapify.ui.screens.HomeScreen
import com.padukarafi.rapify.ui.screens.LibraryScreen
import com.padukarafi.rapify.ui.screens.ReportScreen
import com.padukarafi.rapify.ui.screens.SearchScreen
import com.padukarafi.rapify.ui.theme.DarkBorder
import com.padukarafi.rapify.ui.theme.DarkCard
import com.padukarafi.rapify.ui.theme.RapifyTheme
import com.padukarafi.rapify.ui.theme.MutedText
import com.padukarafi.rapify.ui.theme.PureBlack
import com.padukarafi.rapify.ui.theme.PureWhite
import com.padukarafi.rapify.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    /**
     * AppCompat's per-app locales only reach AppCompatActivity on Android 12 and below.
     * Since Rapify uses ComponentActivity (platform theme), apply the saved language here.
     */
    override fun attachBaseContext(newBase: android.content.Context) {
        val language = try {
            com.padukarafi.rapify.data.settings.SettingsManager.init(newBase).language.value.code
        } catch (_: Exception) {
            ""
        }
        if (language.isEmpty()) {
            super.attachBaseContext(newBase)
            return
        }
        val locale = java.util.Locale(language)
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            RapifyTheme(themeMode = themeMode) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MusicViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isLoadingTrack by viewModel.isLoadingTrack.collectAsState()
    val isExpanded by viewModel.isPlayerExpanded.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    val context = LocalContext.current
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        if (!UpdateManager.hasCheckedThisSession) {
            UpdateManager.hasCheckedThisSession = true
            val update = UpdateManager.getInstance(context).checkForUpdate(force = false)
            if (update != null) {
                availableUpdate = update
            }
        }
    }

    BackHandler(enabled = isExpanded) {
        viewModel.expandPlayer(false)
    }

    val colors = com.padukarafi.rapify.ui.theme.RapifyAppTheme.colors

    val coverTint = rememberCoverTint(currentTrack?.thumbnailUrlForDisplay(192))

    CompositionLocalProvider(LocalCoverTint provides coverTint) {
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        AmbientBackdrop(
            imageUrl = currentTrack?.thumbnailUrlForDisplay(192),
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                GlassNavBar(
                    items = Screen.bottomNavItems,
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
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
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { fadeIn(tween(220, delayMillis = 90)) },
                    exitTransition = { fadeOut(tween(90)) },
                    popEnterTransition = { fadeIn(tween(220, delayMillis = 90)) },
                    popExitTransition = { fadeOut(tween(90)) }
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(viewModel = viewModel)
                    }
                    composable(Screen.Search.route) {
                        SearchScreen(viewModel = viewModel)
                    }
                    composable(Screen.Library.route) {
                        LibraryScreen(viewModel = viewModel)
                    }
                    composable(Screen.Report.route) {
                        ReportScreen()
                    }
                }

                if (currentTrack != null && !isExpanded) {
                    MiniPlayerContainer(
                        viewModel = viewModel,
                        track = currentTrack!!,
                        isPlaying = isPlaying,
                        isLoading = isLoadingTrack,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded && currentTrack != null,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            )
        ) {
            currentTrack?.let { track ->
                FullPlayerSheet(
                    track = track,
                    viewModel = viewModel,
                    isPlaying = isPlaying,
                    isLoading = isLoadingTrack,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onSkipNextClick = { viewModel.skipNext() },
                    onSkipPreviousClick = { viewModel.skipPrevious() },
                    onShuffleClick = { viewModel.toggleShuffle() },
                    onRepeatClick = { viewModel.toggleRepeat() },
                    onFavoriteClick = { viewModel.toggleFavorite(track) },
                    onDismiss = { viewModel.expandPlayer(false) }
                )
            }
        }

        if (availableUpdate != null) {
            UpdateDialog(
                updateInfo = availableUpdate!!,
                onDownloadClick = {
                    UpdateManager.getInstance(context).startDownloadAndInstall(availableUpdate!!)
                    availableUpdate = null
                },
                onDismiss = {
                    availableUpdate = null
                }
            )
        }
    }
    }
}

@Composable
fun MiniPlayerContainer(
    viewModel: MusicViewModel,
    track: Track,
    isPlaying: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val currentPosMs by viewModel.playbackPosition.collectAsState()
    val durMs by viewModel.duration.collectAsState()
    val effDur = if (durMs > 0) durMs else (track.durationSeconds * 1000L).coerceAtLeast(1L)
    val progressFloat = (currentPosMs.toFloat() / effDur.toFloat()).coerceIn(0f, 1f)

    MiniPlayer(
        track = track,
        isPlaying = isPlaying,
        isLoading = isLoading,
        progress = progressFloat,
        onPlayPauseClick = { viewModel.togglePlayPause() },
        onSkipNextClick = { viewModel.skipNext() },
        onClick = { viewModel.expandPlayer(true) },
        modifier = modifier
    )
}
