package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.dropbox.DropboxApi
import de.sroeck.tracksbackend.fit2gpx.FitGpxService
import de.sroeck.tracksbackend.gpxreduce.GpxReduceService
import de.sroeck.tracksbackend.gpxreduce.ReduceSize
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@Service
class TrackService(
    @Autowired private val trackRepository: TrackRepository,
    @Autowired private val dropboxApi: DropboxApi,
    @Autowired private val fitGpxService: FitGpxService,
    @Autowired private val gpxReduceService: GpxReduceService,
) {

    fun listAll(): List<TrackMetaData> {
        return trackRepository.findAll().toList()
    }

    fun getTrack(id: String): TrackEntity? {
        return trackRepository.findByTrackId(id).orElse(null)
    }

    fun getTrackDetailGpxData(id: String): String? {
        return getTrack(id)?.gpxDataDetailXml
    }

    fun getTrackPreviewGpxData(id: String): String? {
        return getTrack(id)?.gpxDataPreviewXml
    }

    fun deleteAllTracks() {
        trackRepository.deleteAll()
    }

    fun fetchNewTracksFromDropboxAndPersistThem() {
        val elapsed = measureTime {
            val existingTracks = measureTimedValue { trackRepository.findAll() }
            val knownDropboxIds = existingTracks.value.map { it.dropboxId }.toSet()
            println("Reading ${knownDropboxIds.size} already persisted tracks from database took ${existingTracks.duration.inWholeMilliseconds}ms")

            val dropboxTracks = measureTimedValue { dropboxApi.fetchTracks() }
            val newDropboxTracks = dropboxTracks.value.filterNot { knownDropboxIds.contains(it.id) }
            println("Identifying ${newDropboxTracks.size} new Tracks from Dropbox took ${dropboxTracks.duration.inWholeMilliseconds}ms")

            newDropboxTracks.forEach { dropboxTrack ->
                println("Processing track ${dropboxTrack.id} / ${dropboxTrack.path} / ${dropboxTrack.name}")
                val bytes = dropboxApi.downloadTrack(dropboxTrack.path)
                println("Downloaded ${bytes.size / 1024}kb")
                val fitData = fitGpxService.parseAsFit(bytes)
                val trackTimestamp = fitData.fitSession.trackTimestamp()
                val trackName = removeTimestamp(dropboxTrack.name.replace(".fit", ""))
                val gpxTrack = fitGpxService.convertToGpx(fitData, trackName)

                val gpxTrackPreview = gpxReduceService.reduceGpx(gpxTrack, ReduceSize.SMALL)
                val gpxTrackDetail = gpxReduceService.reduceGpx(gpxTrack, ReduceSize.MEDIUM)
                println("Reduced #points for track ${gpxTrack.name} (${gpxTrack.trkseg.size}) to preview (${gpxTrackPreview.trkseg.size}) and detail (${gpxTrackDetail.trkseg.size})")
                val entity =
                    TrackEntity(
                        trackId = trackId(trackTimestamp),
                        trackName = gpxTrack.name,
                        dropboxId = dropboxTrack.id,
                        trackTimestamp = trackTimestamp,
                        totalTimerTime = fitData.fitSession.totalTimerTime,
                        totalElapsedTime = fitData.fitSession.totalElapsedTime,
                        totalAscent = fitData.fitSession.totalAscent,
                        totalDescent = fitData.fitSession.totalDescent,
                        totalCalories = fitData.fitSession.totalCalories,
                        totalDistance = fitData.fitSession.totalDistance,
                        gpxDataOriginal = gpxTrack,
                        gpxDataPreview = gpxTrackPreview,
                        gpxDataDetail = gpxTrackDetail,
                    )

                println("Persisting new track id:${entity.trackId} name:${entity.trackName} timestamp:${entity.trackTimestamp}")
                trackRepository.save(entity)
            }
        }

        println("Syncing tracks with Dropbox took ${elapsed.inWholeMilliseconds}ms")
    }

    // 2024-05-09 08:17 Neckarsteig, Etappe 3 und 4
    private fun removeTimestamp(dropboxName: String): String {
        val regex = Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}) (.*)")
        if (regex.containsMatchIn(dropboxName)) {
            return regex.find(dropboxName)!!.groupValues[2]
        }
        return dropboxName
    }

    private fun trackId(trackTimestamp: Instant): String {
        return DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").withZone(ZoneId.of("Z"))
            .format(trackTimestamp)
    }
}