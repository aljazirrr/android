package com.radiowave.app.core.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.radiowave.app.core.domain.model.RadioStation

data class RadioStationDto(
    @SerializedName("stationuuid") val stationUuid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("url_resolved") val urlResolved: String = "",
    @SerializedName("homepage") val homepage: String = "",
    @SerializedName("favicon") val favicon: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("countrycode") val countryCode: String = "",
    @SerializedName("language") val language: String = "",
    @SerializedName("tags") val tags: String = "",
    @SerializedName("votes") val votes: Int = 0,
    @SerializedName("codec") val codec: String = "",
    @SerializedName("bitrate") val bitrate: Int = 0,
    @SerializedName("clickcount") val clickCount: Int = 0,
    @SerializedName("clicktrend") val clickTrend: Int = 0
) {
    fun toDomain(isFavorite: Boolean = false) = RadioStation(
        stationUuid = stationUuid,
        name = name.trim(),
        url = url,
        urlResolved = urlResolved.ifEmpty { url },
        homepage = homepage,
        favicon = favicon,
        country = country,
        countryCode = countryCode,
        language = language,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        votes = votes,
        codec = codec,
        bitrate = bitrate,
        clickCount = clickCount,
        clickTrend = clickTrend,
        isFavorite = isFavorite
    )
}
