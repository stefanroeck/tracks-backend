package de.sroeck.tracksbackend

import GpxTrk
import de.sroeck.tracksbackend.dropbox.DropboxApi
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackRestController(val dropboxApi: DropboxApi, val trackService: TrackService) {

    @GetMapping("/tracks/{id}")
    fun getTrack(@PathVariable id: String): String {
        return "Hello Tracks $id"
    }

    @GetMapping("/tracks")
    fun getTracks(): List<DropboxApi.DropboxTracks> {
        return dropboxApi.fetchTracks()
    }

    @GetMapping("/tracks/{id}/download")
    fun downloadTrack(@PathVariable id: String): ByteArray? {
        return dropboxApi.downloadTrack(id)
    }

    @GetMapping("/cachedTracks", produces = ["application/json"])
    fun cachedTracks(): List<GpxTrk> {
        return trackService.listAll();
    }
}