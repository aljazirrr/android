package com.radiowave.app.core.data.remote

import com.radiowave.app.core.data.remote.dto.RadioStationDto
import retrofit2.http.GET
import retrofit2.http.Query

interface RadioBrowserApi {

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("tag") tag: String? = null,
        @Query("country") country: String? = null,
        @Query("language") language: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStationDto>

    @GET("json/stations/topclick")
    suspend fun getTopStations(
        @Query("limit") limit: Int = 50,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStationDto>

    @GET("json/stations/topvote")
    suspend fun getTopVotedStations(
        @Query("limit") limit: Int = 50,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStationDto>

    @GET("json/stations/lastchange")
    suspend fun getRecentStations(
        @Query("limit") limit: Int = 30,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStationDto>

    @GET("json/stations/byuuid")
    suspend fun getStationByUuid(
        @Query("uuids") uuid: String
    ): List<RadioStationDto>

    @GET("json/tags")
    suspend fun getTags(
        @Query("limit") limit: Int = 100,
        @Query("order") order: String = "stationcount",
        @Query("reverse") reverse: Boolean = true
    ): List<TagDto>

    @GET("json/countries")
    suspend fun getCountries(
        @Query("order") order: String = "stationcount",
        @Query("reverse") reverse: Boolean = true
    ): List<CountryDto>

    @GET("json/stations/bytag")
    suspend fun getStationsByTag(
        @Query("tag") tag: String,
        @Query("limit") limit: Int = 50,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true
    ): List<RadioStationDto>

    @GET("json/stations/bycountry")
    suspend fun getStationsByCountry(
        @Query("country") country: String,
        @Query("limit") limit: Int = 50,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true
    ): List<RadioStationDto>
}

data class TagDto(
    val name: String = "",
    val stationcount: Int = 0
)

data class CountryDto(
    val name: String = "",
    val stationcount: Int = 0,
    val iso_3166_1: String = ""
)
