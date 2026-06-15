package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlaylistEntity
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun PlaylistsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val currentPlaylistId by viewModel.currentPlaylistId.collectAsState()
    val currentPlaylistSongs by viewModel.currentPlaylistSongs.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val currentSong by viewModel.playbackManager.currentSong.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val activePlaylistRef = remember(currentPlaylistId, playlists) {
        playlists.find { it.id == currentPlaylistId }
    }

    AnimatedContent(
        targetState = currentPlaylistId,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "playlists_navigation"
    ) { activeId ->
        if (activeId == null) {
            // Display lists of Playlists
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Smart Playlists Manager",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add and index track listings offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }

                    // Floating Card style trigger button to add playlist
                    FilledTonalButton(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DeepViolet,
                            contentColor = CyberCyan
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (playlists.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.LibraryMusic,
                                text = "No playlists found. Click Create button to start building your mix collections!"
                            )
                        }
                    } else {
                        items(playlists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(12.dp)
                                    .clickable { viewModel.viewPlaylist(playlist.id) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .glassmorphic(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = NeonPink,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Smart collection",
                                        color = MutedSlate,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deletePlaylist(playlist.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Playlist",
                                            tint = MutedSlate,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedSlate)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Display Songs of Selected Playlist
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.viewPlaylist(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyberCyan)
                    }
                    Text(
                        text = activePlaylistRef?.name ?: "Playlist Songs",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Header control card
                if (currentPlaylistSongs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphic(16.dp)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Smart Playlist Queue",
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${currentPlaylistSongs.size} tracks total",
                                    color = MutedSlate,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.playAll(currentPlaylistSongs) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = CosmicBg
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play all")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Playlist Songs lazy column lists
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (currentPlaylistSongs.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.LibraryMusic,
                                text = "No songs added to this playlist yet.\nBrowse tracks from home and add them."
                            )
                        }
                    } else {
                        items(currentPlaylistSongs, key = { it.id }) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(12.dp)
                                    .clickable { viewModel.playSong(song, currentPlaylistSongs) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .glassmorphic(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (song.id == currentSong?.id) CyberCyan else MutedSlate
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        color = if (song.id == currentSong?.id) CyberCyan else TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = song.artist,
                                        color = MutedSlate,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = { 
                                        activePlaylistRef?.let { playlist ->
                                            viewModel.removeSongFromPlaylist(playlist.id, song.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = NeonPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Creating playlist modal dialog popup
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist", color = TextLight, fontWeight = FontWeight.Bold) },
            containerColor = SurfaceDark,
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.trim().isNotEmpty()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = MutedSlate)
                }
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("e.g. Midnight Serenade", color = MutedSlate) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = LightTranslucent,
                        focusedContainerColor = GlassCardBg,
                        unfocusedContainerColor = GlassCardBg
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        )
    }
}
