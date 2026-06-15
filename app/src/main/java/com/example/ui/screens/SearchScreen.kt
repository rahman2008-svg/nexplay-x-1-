package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val songs by viewModel.filteredSongs.collectAsState()
    val currentSong by viewModel.playbackManager.currentSong.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val hotTags = listOf("Prince AR", "Pulse", "Lofi", "Focus", "Ambient", "Morning")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Smart Library Search",
            style = MaterialTheme.typography.titleMedium,
            color = TextLight,
            fontWeight = FontWeight.Bold
        )

        // Custom Styled Neon Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            placeholder = { Text("Search songs, artists, folders...", color = MutedSlate) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = NeonPink)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = LightTranslucent,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = GlassCardBg
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
        )

        // Suggestion Tags Row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Fast Suggestions",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hotTags.take(5).forEach { tag ->
                    val isSelected = query.equals(tag, true)
                    Box(
                        modifier = Modifier
                            .glassmorphic(20.dp)
                            .clickable {
                                if (isSelected) viewModel.setSearchQuery("") else viewModel.setSearchQuery(tag)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            color = if (isSelected) CyberCyan else TextLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Search Results List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (songs.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.MusicNote,
                        text = if (query.isEmpty()) "Type above or choose suggestions to query your smart music cache." else "No scanned tracks matching '$query'."
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
}
