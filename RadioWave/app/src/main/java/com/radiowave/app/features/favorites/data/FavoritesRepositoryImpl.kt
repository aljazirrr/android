package com.radiowave.app.features.favorites.data

import com.radiowave.app.core.data.local.dao.FavoriteStationDao
import com.radiowave.app.core.data.local.entities.FavoriteStationEntity
import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.features.favorites.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoriteStationDao
) : FavoritesRepository {

    override fun getFavorites(): Flow<List<RadioStation>> =
        dao.getAllFavorites().map { entities ->
            entities.map { it.toRadioStation() }
        }

    override suspend fun addFavorite(station: RadioStation) {
        dao.insertFavorite(
            FavoriteStationEntity(
                stationUuid = station.stationUuid,
                name = station.name,
                url = station.url,
                urlResolved = station.urlResolved,
                homepage = station.homepage,
                favicon = station.favicon,
                country = station.country,
                countryCode = station.countryCode,
                language = station.language,
                tags = station.tags.joinToString(","),
                votes = station.votes,
                codec = station.codec,
                bitrate = station.bitrate
            )
        )
    }

    override suspend fun removeFavorite(stationUuid: String) {
        dao.deleteFavorite(stationUuid)
    }

    override suspend fun isFavorite(stationUuid: String): Boolean =
        dao.isFavorite(stationUuid)

    private fun FavoriteStationEntity.toRadioStation() = RadioStation(
        stationUuid = stationUuid,
        name = name,
        url = url,
        urlResolved = urlResolved,
        homepage = homepage,
        favicon = favicon,
        country = country,
        countryCode = countryCode,
        language = language,
        tags = tags.split(",").filter { it.isNotEmpty() },
        votes = votes,
        codec = codec,
        bitrate = bitrate,
        clickCount = 0,
        clickTrend = 0,
        isFavorite = true
    )
}
