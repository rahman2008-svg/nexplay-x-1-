package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.filteredSongs.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val allSongsCount = allSongs.size
    val curMoodFilter by viewModel.selectedMoodFilter.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val currentSong by viewModel.playbackManager.currentSong.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Profile Card
        item {
            WelcomeProfileCard(viewModel)
        }

        // Mood Selection Section
        item {
            MoodEngineSection(
                selectedMood = curMoodFilter,
                onMoodSelected = { viewModel.setMoodFilter(it) }
            )
        }

        // Smart Automations / Offline Engines
        item {
            SmartAutomationsSection(viewModel)
        }

        // Songs Header with details
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (curMoodFilter != null) "$curMoodFilter Vibe Tracks" else "All Scanned Music",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total $allSongsCount device songs found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate
                    )
                }

                if (curMoodFilter != null) {
                    TextButton(
                        onClick = { viewModel.setMoodFilter(null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = CyberCyan)
                    ) {
                        Text("Clear Mood Filter")
                    }
                }
            }
        }

        // Scanned Songs List
        if (songs.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.MusicNote,
                    text = "No tracks found matching filter.\nTry scanning device or clearing filters."
                )
            }
        } else {
            items(songs, key = { it.id }) { song ->
                SongRowItem(
                    song = song,
                    isCurrent = song.id == currentSong?.id,
                    isPlaying = song.id == currentSong?.id && isPlaying,
                    onPlayClick = { viewModel.playSong(song, songs) },
                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                    onAddToNext = { viewModel.playbackManager.addToNext(song) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun WelcomeProfileCard(viewModel: MusicViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(16.dp)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Branded Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(DeepViolet, NeonPink)))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRINCE MODE ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextLight,
                        letterSpacing = 1.sp
                    )
                }

                // Subtitle/Signature
                Text(
                    text = "Designed by Prince",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Welcome, Prince AR Abdur Rahman 🎧",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )

            Text(
                text = "NexVora Lab’s Ofc Ecosystem Hub • Smart Offline Core is active and tracking play cycles dynamically.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun MoodEngineSection(
    selectedMood: String?,
    onMoodSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Smart Music Mood Engine",
            style = MaterialTheme.typography.titleSmall,
            color = TextLight,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val moods = listOf(
                Triple("Chill", Icons.Filled.Cloud, NeonPink),
                Triple("Energy", Icons.Filled.ElectricBolt, CyberCyan),
                Triple("Focus", Icons.Filled.MenuBook, DeepViolet)
            )

            moods.forEach { (name, icon, themeColor) ->
                val isActive = selectedMood == name
                val stateColor by animateColorAsState(
                    targetValue = if (isActive) themeColor else GlassCardBg,
                    animationSpec = tween(300)
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(stateColor)
                        .clickable {
                            if (isActive) onMoodSelected(null) else onMoodSelected(name)
                        }
                        .border(
                            width = 1.dp,
                            color = if (isActive) themeColor else LightTranslucent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = if (isActive) CosmicBg else themeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = name,
                        color = if (isActive) CosmicBg else TextLight,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SmartAutomationsSection(viewModel: MusicViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Smart Day mix card
        Box(
            modifier = Modifier
                .weight(1f)
                .glassmorphic(14.dp)
                .clickable { viewModel.playTimeBasedSmartMix() }
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Time Smart Mix",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Plays energetic core mornings / serene tracks nights.",
                    color = MutedSlate,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }

        // Folder Smart Mix Card
        Box(
            modifier = Modifier
                .weight(1f)
                .glassmorphic(14.dp)
                .clickable { viewModel.playFolderSmartMix() }
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = null,
                    tint = NeonPink,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Folder Automator",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Autogenerates seamless queues from folder pools.",
                    color = MutedSlate,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun SongRowItem(
    song: SongEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToNext: () -> Unit,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    val playlists by viewModel.playlists.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphic(12.dp)
            .clickable { onPlayClick() }
            .padding(10.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Vinyl placeholder
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(DeepViolet, CosmicBg)))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent && isPlaying) {
                // Simple waveform animation
                WaveformAnimationItem(CyberCyan)
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) CyberCyan else MutedSlate,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center labels
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = if (isCurrent) CyberCyan else TextLight,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song.artist,
                    color = MutedSlate,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = "•",
                    color = MutedSlate,
                    fontSize = 10.sp
                )
                // Folder name tag
                Text(
                    text = song.folder,
                    color = NeonPink,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Heart Icon
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (song.isFavorite) NeonPink else MutedSlate,
                modifier = Modifier.size(20.dp)
            )
        }

        // More Dots for Quick Actions Menu
        Box {
            IconButton(
                onClick = { expandedMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MutedSlate,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                modifier = Modifier.background(SurfaceDark)
            ) {
                DropdownMenuItem(
                    text = { Text("Enqueue Next", color = TextLight) },
                    onClick = {
                        onAddToNext()
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Queue, contentDescription = null, tint = CyberCyan) }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist...", color = TextLight) },
                    onClick = {
                        showAddToPlaylistDialog = true
                        expandedMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = CyberCyan) }
                )
            }
        }
    }

    // Add to Playlist picker dialogue
    if (showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = { Text("Select Playlist", color = TextLight) },
            containerColor = SurfaceDark,
            confirmButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("Close", color = CyberCyan)
                }
            },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists found. Create one in the Playlists tab!", color = MutedSlate)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSongToPlaylist(playlist.id, song.id)
                                        showAddToPlaylistDialog = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = NeonPink)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(playlist.name, color = TextLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun WaveformAnimationItem(color: Color) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition()

        val h1 by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, delayMillis = 0, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        val h2 by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(350, delayMillis = 100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        val h3 by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, delayMillis = 50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(modifier = Modifier.width(3.dp).fillMaxHeight(h1).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(h2).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(h3).clip(CircleShape).background(color))
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MutedSlate.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            color = MutedSlate,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
