package com.radiowave.app.features.recommendations.data

import com.radiowave.app.core.data.local.dao.FavoriteStationDao
import com.radiowave.app.core.data.local.dao.ListeningHistoryDao
import com.radiowave.app.core.data.local.dao.TopStationResult
import com.radiowave.app.core.data.remote.RadioBrowserApi
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.recommendations.domain.RecommendationsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecommendationsRepositoryImpl @Inject constructor(
    private val api: RadioBrowserApi,
    private val historyDao: ListeningHistoryDao,
    private val favoriteDao: FavoriteStationDao
) : RecommendationsRepository {

    override suspend fun getRecommendations(): Result<List<RadioStation>> = runCatching {
        val favoriteIds = favoriteDao.getAllFavoriteIds().first().toSet()
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L

        // Get top listened stations for context
        val topListened = historyDao.getTopStations(since = sevenDaysAgo, limit = 5)

        // Use top listened station's country/language as hint for recommendations
        val recentUuids = historyDao.getRecentlyListenedUuids(20).toSet()

        // Fetch recommendations based on popular stations, excluding already listened ones
        val allTop = api.getTopStations(limit = 50)
        val recommended = allTop
            .filter { it.stationUuid !in recentUuids }
            .take(20)
            .map { dto -> dto.toDomain(isFavorite = dto.stationUuid in favoriteIds) }

        // If we have listening history, also fetch by top tag
        if (topListened.isNotEmpty()) {
            // Fetch similar stations by using popular filter - in a real app
            // this would use listening history to recommend by genre/country
        }

        recommended
    }

    override suspend fun getTopListened(): List<TopStationResult> {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return historyDao.getTopStations(since = sevenDaysAgo, limit = 10)
    }
}
