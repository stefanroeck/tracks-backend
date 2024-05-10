package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import de.sroeck.tracksbackend.fit2gpx.convertGpxToString
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Duration.ofDays

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

    private fun gpxXmlResponse(loader: () -> GpxTrk?): ResponseEntity<String> {
        val gpxData = loader() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(ofDays(30)))
            .body(convertGpxToString(gpxData))
    }

    @DeleteMapping("/tracks")
    fun deleteAllTracks() {
        trackService.deleteAllTracks()
    }
}