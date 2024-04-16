package de.sroeck.tracksbackend

import com.fasterxml.jackson.annotation.JsonIgnore
import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import java.time.Instant

data class TrackEntity(
    @Id val trackId: String,
    val trackName: String,
    val dropboxId: String,
    val trackTimestamp: Instant,
    @JsonIgnore val gpxData: GpxTrk
)

interface TrackRepository : CrudRepository<TrackEntity, String> {
}