package de.sroeck.tracksbackend

import com.fasterxml.jackson.annotation.JsonIgnore
import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import de.sroeck.tracksbackend.fit2gpx.convertGpxToString
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.util.*

data class Bounds(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)

private fun boundsFrom(gpxTrack: GpxTrk): Bounds {
    val minLat = gpxTrack.trkseg.minOfOrNull { it.lat } ?: 0.0
    val maxLat = gpxTrack.trkseg.maxOfOrNull { it.lat } ?: 0.0
    val minLon = gpxTrack.trkseg.minOfOrNull { it.lon } ?: 0.0
    val maxLon = gpxTrack.trkseg.maxOfOrNull { it.lon } ?: 0.0
    return Bounds(minLat, maxLat, minLon, maxLon)
}

data class Weather(val temperature: String, val weatherSymbol: String)

interface TrackMetaData {
    val trackId: String
    val trackName: String
    val dropboxId: String
    val trackTimestamp: Instant
    val bounds: Bounds
    val totalElapsedTime: Float
    val totalTimerTime: Float
    val totalDistance: Float
    val totalAscent: Int
    val totalDescent: Int
    val totalCalories: Int
    val weather: Weather
}

@Table(name = "TRACK")
class TrackEntity(
    @Id
    val internalTrackId: Int? = null,
    override val trackId: String,
    override val trackName: String,
    override val dropboxId: String,
    override val trackTimestamp: Instant,
    override val totalElapsedTime: Float,
    override val totalTimerTime: Float,
    override val totalDistance: Float,
    override val totalAscent: Int,
    override val totalDescent: Int,
    override val totalCalories: Int,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) override val bounds: Bounds,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) override val weather: Weather,
    @JsonIgnore val gpxDataOriginalXml: String?,
    @JsonIgnore val gpxDataPreviewXml: String?, // reduced set of points
    @JsonIgnore val gpxDataDetailXml: String?, // more detailed set of points
) : TrackMetaData {
    constructor(
        trackId: String,
        trackName: String,
        dropboxId: String,
        trackTimestamp: Instant,
        totalElapsedTime: Float,
        totalTimerTime: Float,
        totalDistance: Float,
        totalAscent: Int,
        totalDescent: Int,
        totalCalories: Int,
        gpxDataOriginal: GpxTrk,
        gpxDataPreview: GpxTrk,
        gpxDataDetail: GpxTrk,
        weather: Weather,
    ) : this(
        internalTrackId = null,
        trackId = trackId,
        trackName = trackName,
        dropboxId = dropboxId,
        trackTimestamp = trackTimestamp,
        totalElapsedTime = totalElapsedTime,
        totalTimerTime = totalTimerTime,
        totalDistance = totalDistance,
        totalAscent = totalAscent,
        totalDescent = totalDescent,
        totalCalories = totalCalories,
        bounds = boundsFrom(gpxDataPreview),
        weather = weather,
        gpxDataOriginalXml = convertGpxToString(gpxDataOriginal),
        gpxDataPreviewXml = convertGpxToString(gpxDataPreview),
        gpxDataDetailXml = convertGpxToString(gpxDataDetail),
    )
}

interface TrackRepository : CrudRepository<TrackEntity, String> {
    fun findByTrackId(trackId: String): Optional<TrackEntity>
}