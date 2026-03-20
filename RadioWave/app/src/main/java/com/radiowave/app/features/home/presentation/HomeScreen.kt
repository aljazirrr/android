package com.radiowave.app.features.home.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.ui.components.*
import com.radiowave.app.core.ui.theme.*

@Composable
fun HomeScreen(
    onStationClick: (RadioStation) -> Unit,
    onSearchClick: () -> Unit,
    onTagClick: (String) -> Unit,
    onCountryClick: (String) -> Unit,
    currentPlayingId: String?,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Radio,
                            null,
                            tint = RadioWaveGradientStart,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "RadioWave",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.loadData() }
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Hero Banner
                item {
                    HeroBanner()
                }

                // Featured tags
                if (uiState.topTags.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Browse by Genre")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.topTags.take(15)) { tag ->
                                GenreChip(
                                    name = tag.name,
                                    count = tag.stationcount,
                                    onClick = { onTagClick(tag.name) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Top stations
                if (uiState.topStations.isNotEmpty()) {
                    item { SectionHeader(title = "Top Stations", icon = Icons.Default.TrendingUp) }
                    items(uiState.topStations.take(8)) { station ->
                        RadioStationCard(
                            station = station,
                            isPlaying = station.stationUuid == currentPlayingId,
                            onClick = { onStationClick(station) },
                            onFavoriteToggle = {},
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                // Countries
                if (uiState.topCountries.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(title = "Browse by Country", icon = Icons.Default.Public)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.topCountries.take(20)) { country ->
                                CountryChip(
                                    country = country.name,
                                    stationCount = country.stationcount,
                                    onClick = { onCountryClick(country.name) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Trending
                if (uiState.trendingStations.isNotEmpty()) {
                    item { SectionHeader(title = "Trending Now", icon = Icons.Default.Whatshot) }
                    items(uiState.trendingStations.take(6)) { station ->
                        RadioStationCard(
                            station = station,
                            isPlaying = station.stationUuid == currentPlayingId,
                            onClick = { onStationClick(station) },
                            onFavoriteToggle = {},
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(RadioWaveGradientStart, RadioWaveGradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Radio,
                null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "50,000+ Radio Stations",
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "From every corner of the world",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun GenreChip(name: String, count: Int, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(14.dp))
        }
    )
}

@Composable
private fun CountryChip(country: String, stationCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Public,
                null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                country,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "$stationCount stations",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
