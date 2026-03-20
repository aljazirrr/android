package com.radiowave.app.features.home.data

import com.radiowave.app.core.data.local.dao.FavoriteStationDao
import com.radiowave.app.core.data.remote.CountryDto
import com.radiowave.app.core.data.remote.RadioBrowserApi
import com.radiowave.app.core.data.remote.TagDto
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.home.domain.HomeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: RadioBrowserApi,
    private val favoriteDao: FavoriteStationDao
) : HomeRepository {

    override suspend fun getTopStations(): Result<List<RadioStation>> = runCatching {
        val favoriteIds = favoriteDao.getAllFavoriteIds().first().toSet()
        api.getTopStations(limit = 30).map { dto ->
            dto.toDomain(isFavorite = dto.stationUuid in favoriteIds)
        }
    }

    override suspend fun getTrendingStations(): Result<List<RadioStation>> = runCatching {
        val favoriteIds = favoriteDao.getAllFavoriteIds().first().toSet()
        api.getTopVotedStations(limit = 20).map { dto ->
            dto.toDomain(isFavorite = dto.stationUuid in favoriteIds)
        }
    }

    override suspend fun getRecentStations(): Result<List<RadioStation>> = runCatching {
        val favoriteIds = favoriteDao.getAllFavoriteIds().first().toSet()
        api.getRecentStations(limit = 20).map { dto ->
            dto.toDomain(isFavorite = dto.stationUuid in favoriteIds)
        }
    }

    override suspend fun getTopTags(): Result<List<TagDto>> = runCatching {
        api.getTags(limit = 30)
    }

    override suspend fun getTopCountries(): Result<List<CountryDto>> = runCatching {
        api.getCountries().take(30)
    }
}
