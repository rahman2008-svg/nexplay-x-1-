package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MusicViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.data.SongEntity
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainDashboardScreen()
            }
        }
    }
}

@Composable
fun MainDashboardScreen(
    viewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val showPlayerSheet by viewModel.showPlayerSheet.collectAsState()
    val currentSong by viewModel.playbackManager.currentSong.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()

    // Determine target permission dynamically based on Android SDK build versions
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        viewModel.syncLibrary()
    }

    // Automatically trigger permission popups and sync library
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(storagePermission)
        } else {
            viewModel.syncLibrary()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBg)
    ) {
        // Aesthetic Glowing Ambient Orbs background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030712),
                            Color(0xFF0B0F19)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // Prevent gesture overlays
                ) {
                    // Floating Mini-Playback Bar directly above navigation
                    AnimatedVisibility(
                        visible = currentSong != null,
                        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(),
                        exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut()
                    ) {
                        currentSong?.let { song ->
                            MiniPlayerBar(
                                song = song,
                                isPlaying = isPlaying,
                                onBarClick = { viewModel.showPlayer(true) },
                                onPlayPauseClick = { viewModel.playbackManager.togglePlayPause() }
                            )
                        }
                    }

                    // Cyber Neon Navigation Bar
                    BottomNavigationBar(
                        activeTab = activeTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .statusBarsPadding() // Compensate state bar overlays
            ) {
                // If permission is absent, show a friendly notification to prompt access
                if (!hasPermission) {
                    PermissionPromptCard(
                        onGrantClick = { launcher.launch(storagePermission) }
                    )
                } else {
                    // Load active tab panels
                    when (activeTab) {
                        0 -> HomeScreen(viewModel = viewModel)
                        1 -> LibraryScreen(viewModel = viewModel)
                        2 -> SearchScreen(viewModel = viewModel)
                        3 -> PlaylistsScreen(viewModel = viewModel)
                        4 -> EqualizerScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Expanded detailed musical overlays sheet panel
        AnimatedVisibility(
            visible = showPlayerSheet,
            enter = slideInVertically(animationSpec = tween(400)) { it } + fadeIn(),
            exit = slideOutVertically(animationSpec = tween(350)) { it } + fadeOut()
        ) {
            PlayerDetailSheet(
                viewModel = viewModel,
                onClose = { viewModel.showPlayer(false) }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        Triple("Home", Icons.Default.Home, 0),
        Triple("Library", Icons.Default.LibraryMusic, 1),
        Triple("Search", Icons.Default.Search, 2),
        Triple("Playlist", Icons.Default.QueueMusic, 3),
        Triple("Equalizer", Icons.Default.Tune, 4)
    )

    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("app_bottom_nav_bar")
    ) {
        items.forEach { (label, icon, index) ->
            val isSelected = activeTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) CosmicBg else MutedSlate,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) CyberCyan else MutedSlate,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (index % 2 == 0) CyberCyan else NeonPink
                )
            )
        }
    }
}

@Composable
fun MiniPlayerBar(
    song: SongEntity,
    isPlaying: Boolean,
    onBarClick: () -> Unit,
    onPlayPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .glassmorphic(14.dp)
            .clickable { onBarClick() }
            .padding(10.dp)
            .testTag("mini_player_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rotating vinyl logo inside micro player
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(DeepViolet, NeonPink))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = TextLight,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                song.title,
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                song.artist,
                color = CyberCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Large tapping play/pause action
        IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .size(38.dp)
                .background(CyberCyan, shape = CircleShape)
                .testTag("mini_play_pause_button")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = CosmicBg,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PermissionPromptCard(
    onGrantClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(16.dp)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    text = "Storage Access Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "NexPlay X scans your offline storage to build your artists, folders, playlists and custom equalizer profiles securely. Grant access to index your tracks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CosmicBg
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Grant Device Storage Scan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
