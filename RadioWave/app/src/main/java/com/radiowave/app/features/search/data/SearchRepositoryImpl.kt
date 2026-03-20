package com.radiowave.app.features.search.data

import com.radiowave.app.core.data.local.dao.FavoriteStationDao
import com.radiowave.app.core.data.remote.RadioBrowserApi
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.domain.model.SearchFilter
import com.radiowave.app.features.search.domain.SearchRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: RadioBrowserApi,
    private val favoriteDao: FavoriteStationDao
) : SearchRepository {

    override suspend fun search(filter: SearchFilter, offset: Int): Result<List<RadioStation>> =
        runCatching {
            val favoriteIds = favoriteDao.getAllFavoriteIds().first().toSet()
            val dtos = when (filter) {
                is SearchFilter.ByName -> api.searchStations(name = filter.query, offset = offset)
                is SearchFilter.ByTag -> api.getStationsByTag(tag = filter.tag)
                is SearchFilter.ByCountry -> api.getStationsByCountry(country = filter.country)
                is SearchFilter.ByLanguage -> api.searchStations(language = filter.language, offset = offset)
                is SearchFilter.ByPopularity -> api.getTopStations(limit = 50)
                is SearchFilter.ByTrending -> api.getTopVotedStations(limit = 50)
            }
            dtos.map { dto -> dto.toDomain(isFavorite = dto.stationUuid in favoriteIds) }
        }
}
