package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.MusicViewModel
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun PlayerDetailSheet(
    viewModel: MusicViewModel,
    onClose: () -> Unit
) {
    val playbackManager = viewModel.playbackManager
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val position by playbackManager.playbackPosition.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    if (currentSong == null) return

    val song = currentSong!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("player_details_sheet")
    ) {
        // Glowing Neon Background Accents
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerVal = Offset(size.width / 2, size.height / 3)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DeepViolet.copy(alpha = 0.35f), Color.Transparent),
                        center = centerVal,
                        radius = size.width * 0.9f
                    ),
                    center = centerVal,
                    radius = size.width * 0.9f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width, size.height * 0.7f),
                        radius = size.width * 0.6f
                    ),
                    center = Offset(size.width, size.height * 0.7f),
                    radius = size.width * 0.6f
                )
            }
        }

        // Action controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextLight,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        color = MutedSlate,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "NexVora Smart Streamer",
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                IconButton(onClick = { viewModel.setTab(4); onClose() }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Equalizer",
                        tint = TextLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Big CD disc Rotating Vinyl art
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                RotatingVinylDisk(isPlaying = isPlaying)
            }

            // Text Metadata Detail Labels
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song.title,
                    color = TextLight,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    color = CyberCyan,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LightTranslucent)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = song.folder,
                            color = NeonPink,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LightTranslucent)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = song.genre,
                            color = CyberCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Music Waves Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                HIFIWaveVisualizer(isPlaying = isPlaying)
            }

            // Progress Slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Slider(
                    value = position.toFloat().coerceIn(0f, duration.toFloat()),
                    onValueChange = { playbackManager.seekTo(it.toLong()) },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberCyan,
                        inactiveTrackColor = LightTranslucent
                    ),
                    modifier = Modifier.testTag("player_progress_bar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(position),
                        color = MutedSlate,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatTime(duration),
                        color = MutedSlate,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Controls Center Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart favorite song toggle
                IconButton(
                    onClick = { viewModel.toggleFavorite(song) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) NeonPink else TextLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Prev Button
                IconButton(
                    onClick = { playbackManager.playPrevious() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextLight,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause Circle Orb Neon
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(CyberCyan, DeepViolet)))
                        .clickable { playbackManager.togglePlayPause() }
                        .testTag("play_pause_sheet_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = CosmicBg,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = { playbackManager.playNext() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextLight,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Playlist Add toggle
                IconButton(
                    onClick = { viewModel.setTab(3); onClose() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Playlists",
                        tint = TextLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Signature note
            Text(
                text = "NexVora Offline Ecosystem X v1.0",
                color = MutedSlate.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun RotatingVinylDisk(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Smooth color cycles for aesthetic rings
    val glowColor by infiniteTransition.animateColor(
        initialValue = CyberCyan,
        targetValue = NeonPink,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val currentRotation = if (isPlaying) rotationAngle else 0f

    Canvas(
        modifier = Modifier
            .size(260.dp)
            .aspectRatio(1f)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width / 2

        // Draw external energy halo ring
        drawCircle(
            color = glowColor.copy(alpha = 0.15f),
            radius = radius + 8.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw solid Vinyl base (outer disk)
        drawCircle(
            color = Color(0xFF0F1018),
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // Draw standard textured grooves
        for (i in 4..12) {
            drawCircle(
                color = Color(0xFF202334),
                radius = radius * (i / 14f),
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw rotating album center emblem
        rotate(degrees = currentRotation) {
            // Inner Core label fill
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(DeepViolet, NeonPink, CyberCyan)
                ),
                radius = radius * 0.35f,
                center = Offset(centerX, centerY)
            )

            // Dynamic rotating record center lines
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(centerX - radius * 0.32f, centerY),
                end = Offset(centerX + radius * 0.32f, centerY),
                strokeWidth = 2.dp.toPx()
            )

            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(centerX, centerY - radius * 0.32f),
                end = Offset(centerX, centerY + radius * 0.32f),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Center vinyl plastic slot hole
        drawCircle(
            color = Color(0xFF090A0F),
            radius = radius * 0.08f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.03f,
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
fun HIFIWaveVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val currentPhase = if (isPlaying) phaseShift else 0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        val points = 60
        val stepX = width / points

        val pathBrush = Brush.linearGradient(
            colors = listOf(CyberCyan, NeonPink)
        )

        for (i in 0 until points - 1) {
            val x1 = i * stepX
            val x2 = (i + 1) * stepX

            // Normal distribution bell for wave boundaries
            val factor = sin(i.toFloat() / points.toFloat() * Math.PI)

            val amp = if (isPlaying) 15.dp.toPx() else 4.dp.toPx()
            val y1 = midY + sin(i * 0.2f - currentPhase).toFloat() * amp.toFloat() * factor.toFloat()
            val y2 = midY + sin((i + 1) * 0.2f - currentPhase).toFloat() * amp.toFloat() * factor.toFloat()

            drawLine(
                brush = pathBrush,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
