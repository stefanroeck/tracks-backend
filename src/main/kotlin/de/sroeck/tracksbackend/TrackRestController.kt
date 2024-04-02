package de.sroeck.tracksbackend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackRestController(val dropboxApi: DropboxApi) {

    @GetMapping("/tracks/{id}")
    fun getTrack (@PathVariable id: String): String {
        return "Hello Tracks $id"
    }

    @GetMapping("/tracks")
    fun getTracks(): List<DropboxApi.DropboxTracks> {
        return dropboxApi.fetchTracks()
    }
}