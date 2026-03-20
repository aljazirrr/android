package com.radiowave.app.features.player.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.radiowave.app.core.domain.model.PlayerState
import com.radiowave.app.core.ui.components.SoundWaveAnimation
import com.radiowave.app.core.ui.theme.*

@Composable
fun PlayerScreen(
    onDismiss: () -> Unit,
    onRecordClick: () -> Unit,
    onShareClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.playerUiState.collectAsState()
    val station = uiState.currentStation ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkSurface,
                        DarkBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, "Minimize", tint = Color.White)
                }
                Text(
                    "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Default.Share, "Share", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Station logo
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCard),
                contentAlignment = Alignment.Center
            ) {
                if (station.favicon.isNotEmpty()) {
                    AsyncImage(
                        model = station.favicon,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Radio,
                        null,
                        tint = RadioWaveGradientStart,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sound wave
            AnimatedVisibility(visible = uiState.playerState == PlayerState.PLAYING) {
                SoundWaveAnimation(
                    isPlaying = uiState.playerState == PlayerState.PLAYING,
                    barCount = 5,
                    modifier = Modifier.height(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Station info
            Text(
                text = station.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (station.country.isNotEmpty()) {
                    Text(
                        station.country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                if (station.tags.isNotEmpty()) {
                    Text("•", color = Color.White.copy(alpha = 0.4f))
                    Text(
                        station.tags.first().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = RadioWaveGradientEnd
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bitrate badge
            if (station.bitrate > 0) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RadioWaveGradientStart.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${station.bitrate} kbps • ${station.codec}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadioWaveGradientStart,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Volume slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.VolumeDown,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Slider(
                    value = uiState.volume,
                    onValueChange = { viewModel.setVolume(it) },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = RadioWaveGradientStart,
                        activeTrackColor = RadioWaveGradientStart
                    )
                )
                Icon(
                    Icons.Default.VolumeUp,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Record button
                IconButton(
                    onClick = onRecordClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        "Record",
                        tint = RecordingRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play/Pause button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(RadioWaveGradientStart, RadioWaveGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState.playerState) {
                        PlayerState.LOADING, PlayerState.BUFFERING -> CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        PlayerState.PLAYING -> IconButton(onClick = { viewModel.togglePlayPause() }) {
                            Icon(
                                Icons.Default.Pause,
                                "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        else -> IconButton(onClick = { viewModel.togglePlayPause() }) {
                            Icon(
                                Icons.Default.PlayArrow,
                                "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Favorite button
                IconButton(
                    onClick = { viewModel.toggleFavorite() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        if (station.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite",
                        tint = if (station.isFavorite) FavoriteYellow else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val statusColor = when (uiState.playerState) {
                    PlayerState.PLAYING -> PlayingGreen
                    PlayerState.LOADING, PlayerState.BUFFERING -> FavoriteYellow
                    PlayerState.ERROR -> RecordingRed
                    else -> Color.White.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = when (uiState.playerState) {
                        PlayerState.PLAYING -> "Live"
                        PlayerState.LOADING -> "Connecting..."
                        PlayerState.BUFFERING -> "Buffering..."
                        PlayerState.ERROR -> "Connection failed"
                        PlayerState.PAUSED -> "Paused"
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
