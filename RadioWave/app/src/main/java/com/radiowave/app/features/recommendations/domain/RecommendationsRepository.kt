package com.radiowave.app.features.recommendations.domain

import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.data.local.dao.TopStationResult

interface RecommendationsRepository {
    suspend fun getRecommendations(): Result<List<RadioStation>>
    suspend fun getTopListened(): List<TopStationResult>
}
