package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.persistence.TrackEntity
import de.sroeck.tracksbackend.persistence.TrackMetaData
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Duration.ofDays

@RestController
class TrackRestController(val trackService: TrackService) {

    data class TrackBounds(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)
    data class AllTracksResponse(val tracks: List<TrackMetaData>, val allTrackBounds: TrackBounds?)

    @GetMapping("/tracks", produces = ["application/json"])
    fun getTracks(): AllTracksResponse {
        val allTracks = trackService.listAll()
        val bounds = allTracks.map { it.bounds }
        val trackBounds = if (bounds.isNotEmpty()) TrackBounds(
            bounds.minOf { it.minLat },
            bounds.maxOf { it.maxLat },
            bounds.minOf { it.minLon },
            bounds.maxOf { it.maxLon }
        ) else null

        return AllTracksResponse(allTracks, trackBounds)
    }

    @GetMapping("/tracks/{id}", produces = ["application/json"])
    fun getTrack(@PathVariable id: String): TrackEntity? {
        return trackService.getTrack(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping("/tracks/{id}/gpx_detail", produces = ["application/xml"])
    @ResponseBody
    fun gpxDetail(@PathVariable id: String): ResponseEntity<String>? {
        return gpxXmlResponse { trackService.getTrackDetailGpxData(id) }
    }

    @GetMapping("/tracks/{id}/gpx", produces = ["application/xml"])
    @ResponseBody
    fun gpx(@PathVariable id: String): ResponseEntity<String>? {
        return gpxXmlResponse { trackService.getTrackPreviewGpxData(id) }
    }

    private fun gpxXmlResponse(loader: () -> String?): ResponseEntity<String> {
        val gpxData = loader() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(ofDays(30)))
            .body(gpxData)
    }

    @DeleteMapping("/tracks")
    fun deleteAllTracks() {
        trackService.deleteAllTracks()
    }

    data class SyncResponse(val syncedTracks: Int)

    @PostMapping("/tracks/sync")
    fun syncTracks(): SyncResponse {
        trackService.fetchNewTracksFromDropboxAndPersistThem().let {
            return SyncResponse(syncedTracks = it.syncedTracks)
        }
    }
}