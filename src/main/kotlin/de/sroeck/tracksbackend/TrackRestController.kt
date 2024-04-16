package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
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
    fun downloadTrack(@PathVariable id: String): GpxTrk? {
        return trackService.getTrackGpxData(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}