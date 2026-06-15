package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.MusicViewModel
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    // Current nested library view
    // 0: Categories Hub, 1: Artists List, 2: Albums List, 3: Folders List, 4: Favorites List, 5: Single Category Detail
    var currentSubView by remember { mutableStateOf(0) }
    var selectedDetailType by remember { mutableStateOf("") } // "Artist", "Album", "Folder"
    var selectedDetailName by remember { mutableStateOf("") }

    val allSongs by viewModel.allSongs.collectAsState()
    val favorites by viewModel.favoriteSongs.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val currentSong by viewModel.playbackManager.currentSong.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()

    AnimatedContent(
        targetState = currentSubView,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "library_scrolling"
    ) { viewState ->
        when (viewState) {
            0 -> {
                // Main categories hub
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Music Library Hub",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )

                    // Large 2x2 Category grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryHubCard(
                                title = "Artists",
                                countString = "${artists.size} Artists",
                                icon = Icons.Default.Person,
                                color = CyberCyan,
                                modifier = Modifier.weight(1f),
                                onClick = { currentSubView = 1 }
                            )
                            CategoryHubCard(
                                title = "Albums",
                                countString = "${albums.size} Albums",
                                icon = Icons.Default.Album,
                                color = NeonPink,
                                modifier = Modifier.weight(1f),
                                onClick = { currentSubView = 2 }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryHubCard(
                                title = "Folders",
                                countString = "${folders.size} Dynamic Folders",
                                icon = Icons.Default.Folder,
                                color = DeepViolet,
                                modifier = Modifier.weight(1f),
                                onClick = { currentSubView = 3 }
                            )
                            CategoryHubCard(
                                title = "Favorites",
                                countString = "${favorites.size} Hearted Songs",
                                icon = Icons.Default.Favorite,
                                color = Color.Red,
                                modifier = Modifier.weight(1f),
                                onClick = { currentSubView = 4 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recently Added Scans",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )

                    // Quick list of all files in library
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        if (allSongs.isEmpty()) {
                            item {
                                EmptyStateView(Icons.Default.LibraryMusic, "No scanned tracks. Pull from home to refresh.")
                            }
                        } else {
                            items(allSongs.take(15)) { song ->
                                SongRowItem(
                                    song = song,
                                    isCurrent = song.id == currentSong?.id,
                                    isPlaying = song.id == currentSong?.id && isPlaying,
                                    onPlayClick = { viewModel.playSong(song, allSongs) },
                                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                                    onAddToNext = { viewModel.playbackManager.addToNext(song) },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Artists list
                LibraryListSubscreen(
                    title = "Artists",
                    items = artists,
                    onBack = { currentSubView = 0 },
                    icon = Icons.Default.Person,
                    getItemCount = { name -> allSongs.count { it.artist == name } },
                    onItemClick = { name ->
                        selectedDetailType = "Artist"
                        selectedDetailName = name
                        currentSubView = 5
                    }
                )
            }

            2 -> {
                // Albums List
                LibraryListSubscreen(
                    title = "Albums",
                    items = albums,
                    onBack = { currentSubView = 0 },
                    icon = Icons.Default.Album,
                    getItemCount = { name -> allSongs.count { it.album == name } },
                    onItemClick = { name ->
                        selectedDetailType = "Album"
                        selectedDetailName = name
                        currentSubView = 5
                    }
                )
            }

            3 -> {
                // Folders list
                LibraryListSubscreen(
                    title = "Folders",
                    items = folders,
                    onBack = { currentSubView = 0 },
                    icon = Icons.Default.Folder,
                    getItemCount = { name -> allSongs.count { it.folder == name } },
                    onItemClick = { name ->
                        selectedDetailType = "Folder"
                        selectedDetailName = name
                        currentSubView = 5
                    }
                )
            }

            4 -> {
                // Favorites List page
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = { currentSubView = 0 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyberCyan)
                    }
                    Text(
                        text = "My Hearted Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        if (favorites.isEmpty()) {
                            item {
                                EmptyStateView(Icons.Default.FavoriteBorder, "Heart songs during playback\nto fill up your favorites queue.")
                            }
                        } else {
                            items(favorites) { song ->
                                SongRowItem(
                                    song = song,
                                    isCurrent = song.id == currentSong?.id,
                                    isPlaying = song.id == currentSong?.id && isPlaying,
                                    onPlayClick = { viewModel.playSong(song, favorites) },
                                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                                    onAddToNext = { viewModel.playbackManager.addToNext(song) },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }

            5 -> {
                // Detail List representing a single artist / album / folder
                val relevantSongs = remember(selectedDetailType, selectedDetailName, allSongs) {
                    when (selectedDetailType) {
                        "Artist" -> allSongs.filter { it.artist == selectedDetailName }
                        "Album" -> allSongs.filter { it.album == selectedDetailName }
                        "Folder" -> allSongs.filter { it.folder == selectedDetailName }
                        else -> emptyList()
                    }
                }

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = {
                        currentSubView = when (selectedDetailType) {
                            "Artist" -> 1
                            "Album" -> 2
                            "Folder" -> 3
                            else -> 0
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyberCyan)
                    }
                    
                    Text(
                        text = selectedDetailName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "$selectedDetailType category • ${relevantSongs.size} tracks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(relevantSongs) { song ->
                            SongRowItem(
                                song = song,
                                isCurrent = song.id == currentSong?.id,
                                isPlaying = song.id == currentSong?.id && isPlaying,
                                onPlayClick = { viewModel.playSong(song, relevantSongs) },
                                onFavoriteClick = { viewModel.toggleFavorite(song) },
                                onAddToNext = { viewModel.playbackManager.addToNext(song) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHubCard(
    title: String,
    countString: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphic(16.dp)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextLight
            )
            Text(
                text = countString,
                style = MaterialTheme.typography.labelMedium,
                color = MutedSlate
            )
        }
    }
}

@Composable
fun LibraryListSubscreen(
    title: String,
    items: List<String>,
    onBack: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    getItemCount: (String) -> Int,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyberCyan)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            EmptyStateView(icon, "No library groupings found.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { name ->
                    val tracksCount = getItemCount(name)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphic(12.dp)
                            .clickable { onItemClick(name) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("$tracksCount tracks scanned", color = MutedSlate, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedSlate)
                    }
                }
            }
        }
    }
}
