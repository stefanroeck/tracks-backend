package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.fit2gpx.convertGpxToString
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@CrossOrigin(origins = ["http://localhost:8080", "https://gpxtracks.vercel.app/"])
class TrackRestController(val trackService: TrackService) {

    @GetMapping("/tracks", produces = ["application/json"])
    fun getTracks(): List<TrackEntity> {
        return trackService.listAll()
    }

    @GetMapping("/tracks/{id}", produces = ["application/json"])
    fun getTrack(@PathVariable id: String): TrackEntity? {
        return trackService.getTrack(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping("/tracks/{id}/gpx", produces = ["application/xml"])
    fun downloadTrack(@PathVariable id: String): String? {
        val gpxData = trackService.getTrackGpxData(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return convertGpxToString(gpxData)
    }

    @DeleteMapping("/tracks")
    fun deleteAllTracks() {
        trackService.deleteAllTracks()
        trackService.fetchTracksFromDropboxAndPersistThem()
    }
}