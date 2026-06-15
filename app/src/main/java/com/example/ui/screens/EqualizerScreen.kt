package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playbackManager = viewModel.playbackManager
    val bassBoost by playbackManager.bassBoostLevel.collectAsState()
    val virtualizer by playbackManager.virtualizerLevel.collectAsState()
    val activePreset by playbackManager.presetName.collectAsState()
    val eqBands by playbackManager.eqBands.collectAsState()
    val sleepSecondsLeft by playbackManager.sleepTimerSecondsLeft.collectAsState()

    val presets = listOf("Focus", "Pop", "Rock", "Jazz", "Classic", "Flat")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Equalizer Controls Header
        item {
            Text(
                text = "NexVora Equalizer Studio",
                style = MaterialTheme.typography.titleMedium,
                color = TextLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fine-tune and enrich your acoustic offline session",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate
            )
        }

        // Bass & 3D Surround Knobs/Sliders Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(16.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "3D Acoustic Engines",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // Bass Boost slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BASS BOOST", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                            Text("$bassBoost%", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Slider(
                            value = bassBoost.toFloat(),
                            onValueChange = { playbackManager.updateBassBoost(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan,
                                inactiveTrackColor = LightTranslucent
                            )
                        )
                    }

                    // Virtualizer 3D slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3D SURROUND DEPTH", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                            Text("$virtualizer%", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Slider(
                            value = virtualizer.toFloat(),
                            onValueChange = { playbackManager.updateVirtualizer(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonPink,
                                activeTrackColor = NeonPink,
                                inactiveTrackColor = LightTranslucent
                            )
                        )
                    }
                }
            }
        }

        // Equalizer Presets Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Equalizer Presets",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { preset ->
                        PresetButton(
                            name = preset,
                            isActive = activePreset == preset,
                            color = CyberCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { playbackManager.setPreset(preset) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(3).forEach { preset ->
                        PresetButton(
                            name = preset,
                            isActive = activePreset == preset,
                            color = NeonPink,
                            modifier = Modifier.weight(1f),
                            onClick = { playbackManager.setPreset(preset) }
                        )
                    }
                }
            }
        }

        // Custom 5-Band Graphic bars
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(16.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Graphic Band Adjustments",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val frequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
                        eqBands.forEachIndexed { index, value ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            ) {
                                // Vertical slider
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Custom Slider implementation
                                    Slider(
                                        value = value.toFloat(),
                                        onValueChange = { playbackManager.updateBandValue(index, it.toInt()) },
                                        valueRange = 0f..100f,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .height(200.dp)
                                            .width(200.dp), // will be drawn vertical
                                        colors = SliderDefaults.colors(
                                            thumbColor = if (index % 2 == 0) CyberCyan else NeonPink,
                                            activeTrackColor = if (index % 2 == 0) CyberCyan else NeonPink,
                                            inactiveTrackColor = LightTranslucent
                                        )
                                    )
                                }

                                Text(
                                    text = "$value",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight,
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                Text(
                                    text = frequencies[index],
                                    fontSize = 9.sp,
                                    color = MutedSlate,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sleep Timer Selection Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(16.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acoustic Sleep Timer",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        if (sleepSecondsLeft > 0) {
                            val mins = sleepSecondsLeft / 60
                            val secs = sleepSecondsLeft % 60
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonPink.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonPink, modifier = Modifier.size(14.dp))
                                Text(
                                    text = String.format("%02d:%02d left", mins, secs),
                                    color = NeonPink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "Auto-stops playback to conserve energy when sleeping.",
                        color = MutedSlate,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intervals = listOf(
                            Pair("Off", 0),
                            Pair("10m", 10),
                            Pair("20m", 20),
                            Pair("30m", 30),
                            Pair("60m", 60)
                        )

                        intervals.forEach { (label, durationMins) ->
                            val isCurrentTimer = if (durationMins == 0) sleepSecondsLeft <= 0 else (sleepSecondsLeft / 60) == durationMins
                            val targetColor = if (isCurrentTimer) DeepViolet else GlassCardBg

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(targetColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrentTimer) CyberCyan else LightTranslucent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { playbackManager.setSleepTimer(durationMins) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isCurrentTimer) CyberCyan else TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // NexVora Lab Signature
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(16.dp)
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Hexagon,
                        contentDescription = "Signature",
                        tint = CyberCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "NexVora Lab’s Ofc (Product Specs)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Lead Architect Signature: Prince AR Abdur Rahman",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NexVoice OS Integration", color = CyberCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonPink.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Day Planner X Sync", color = NeonPink, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    name: String,
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) color else GlassCardBg)
            .border(
                width = 1.dp,
                color = if (isActive) color else LightTranslucent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isActive) CosmicBg else TextLight,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
