package com.radiowave.app.features.home.domain

import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.data.remote.CountryDto
import com.radiowave.app.core.data.remote.TagDto

interface HomeRepository {
    suspend fun getTopStations(): Result<List<RadioStation>>
    suspend fun getTrendingStations(): Result<List<RadioStation>>
    suspend fun getRecentStations(): Result<List<RadioStation>>
    suspend fun getTopTags(): Result<List<TagDto>>
    suspend fun getTopCountries(): Result<List<CountryDto>>
}
