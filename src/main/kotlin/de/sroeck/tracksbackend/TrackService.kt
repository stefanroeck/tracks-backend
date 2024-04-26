package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.dropbox.DropboxApi
import de.sroeck.tracksbackend.fit2gpx.FitGpxService
import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class TrackService(
    @Autowired private val trackRepository: TrackRepository,
    @Autowired private val dropboxApi: DropboxApi,
    @Autowired private val fitGpxService: FitGpxService,
) {

    fun listAll(): List<TrackEntity> {
        fetchTracksFromDropboxAndPersistThem()

        return trackRepository.findAll().toList()
    }

    fun getTrack(id: String): TrackEntity? {
        return trackRepository.findById(id).orElse(null)
    }

    fun getTrackGpxData(id: String): GpxTrk? {
        return getTrack(id)?.gpxData
    }

    fun deleteAllTracks() {
        trackRepository.deleteAll()
    }

    fun fetchTracksFromDropboxAndPersistThem() {
        val knownDropboxIds = trackRepository.findAll().map { it.dropboxId }.toSet()
        println("Already persisted tracks: ${knownDropboxIds.size}")

        val newDropboxTracks = dropboxApi.fetchTracks().filterNot { knownDropboxIds.contains(it.id) }
        println("New Tracks from Dropbox: ${newDropboxTracks.size}")

        newDropboxTracks.forEach { dropboxTrack ->
            println("Processing track ${dropboxTrack.id} / ${dropboxTrack.path}")
            val bytes = dropboxApi.downloadTrack(dropboxTrack.path)
            println("Downloaded ${bytes.size / 1024}kb")
            val fitData = fitGpxService.parseAsFit(bytes)
            val trackTimestamp = fitData.fitSession.trackTimestamp()
            val gpxTrack = fitGpxService.convertToGpx(fitData)
            val entity = TrackEntity(trackId(trackTimestamp), gpxTrack.name, dropboxTrack.id, trackTimestamp, gpxTrack)

            println("Persisting new track id:${entity.trackId} name:${entity.trackName} timstamp:${entity.trackTimestamp}")
            trackRepository.save(entity)
        }
    }

    private fun trackId(trackTimestamp: Instant): String {
        return DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").withZone(ZoneId.of("Z"))
            .format(trackTimestamp)
    }
}