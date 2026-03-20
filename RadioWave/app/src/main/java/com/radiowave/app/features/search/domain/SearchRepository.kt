package com.radiowave.app.features.search.domain

import com.radiowave.app.core.domain.model.RadioStation
import com.radiowave.app.core.domain.model.SearchFilter

interface SearchRepository {
    suspend fun search(filter: SearchFilter, offset: Int = 0): Result<List<RadioStation>>
}
