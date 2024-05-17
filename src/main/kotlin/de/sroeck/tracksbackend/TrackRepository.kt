package de.sroeck.tracksbackend

import com.fasterxml.jackson.annotation.JsonIgnore
import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.Repository
import java.time.Instant

data class Bounds(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)

private fun boundsFrom(gpxTrack: GpxTrk): Bounds {
    val minLat = gpxTrack.trkseg.minOfOrNull { it.lat } ?: 0.0
    val maxLat = gpxTrack.trkseg.maxOfOrNull { it.lat } ?: 0.0
    val minLon = gpxTrack.trkseg.minOfOrNull { it.lon } ?: 0.0
    val maxLon = gpxTrack.trkseg.maxOfOrNull { it.lon } ?: 0.0
    return Bounds(minLat, maxLat, minLon, maxLon)
}

interface TrackMetaData {
    val trackId: String
    val trackName: String
    val dropboxId: String
    val trackTimestamp: Instant
    val bounds: Bounds
}

class TrackEntity(
    @Id override val trackId: String,
    override val trackName: String,
    override val dropboxId: String,
    override val trackTimestamp: Instant,
    override val bounds: Bounds,
    @JsonIgnore val gpxDataOriginal: GpxTrk?,
    @JsonIgnore val gpxDataPreview: GpxTrk?, // reduced set of points
    @JsonIgnore val gpxDataDetail: GpxTrk?, // more detailed set of points
) : TrackMetaData {
    constructor(
        trackId: String,
        trackName: String,
        dropboxId: String,
        trackTimestamp: Instant,
        gpxDataOriginal: GpxTrk,
        gpxDataPreview: GpxTrk,
        gpxDataDetail: GpxTrk,
    ) : this(
        trackId,
        trackName,
        dropboxId,
        trackTimestamp,
        boundsFrom(gpxDataPreview),
        gpxDataOriginal,
        gpxDataPreview,
        gpxDataDetail
    )
}

interface TrackRepository : Repository<TrackEntity, String> {
    @Query(value = "{}", fields = "{'trackId': 1, 'trackName': 1, 'dropboxId': 1, 'trackTimestamp': 1, 'bounds': 1}")
    fun findAll(): List<TrackMetaData>
    fun findById(id: String): TrackEntity?
    fun deleteAll()
    fun save(track: TrackEntity): TrackEntity
}