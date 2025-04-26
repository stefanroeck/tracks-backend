package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.converter.shared.ConversionContext
import de.sroeck.tracksbackend.converter.shared.GpxConverter
import de.sroeck.tracksbackend.dropbox.DropboxApi
import de.sroeck.tracksbackend.gpxreduce.GpxReduceService
import de.sroeck.tracksbackend.gpxreduce.ReduceSize
import de.sroeck.tracksbackend.persistence.TrackEntity
import de.sroeck.tracksbackend.persistence.TrackMetaData
import de.sroeck.tracksbackend.persistence.TrackRepository
import de.sroeck.tracksbackend.persistence.Weather
import de.sroeck.tracksbackend.weather.WeatherService
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
    @Autowired private val gpxConverterCandidates: List<GpxConverter>,
    @Autowired private val gpxReduceService: GpxReduceService,
    @Autowired private val weatherService: WeatherService,
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

    private fun conversionService(discriminator: String): GpxConverter {
        return gpxConverterCandidates.first { it.canHandle(discriminator) }
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

                val trackName = dropboxTrack.name.removeTimestamp().removeExtension()
                val (gpxTrack, metadata) = conversionService(dropboxTrack.name).convert(
                    bytes,
                    ConversionContext(trackName)
                )
                val trackTimestamp = metadata.trackTimestamp

                val gpxTrackPreview = gpxReduceService.reduceGpx(gpxTrack, ReduceSize.SMALL)
                val gpxTrackDetail = gpxReduceService.reduceGpx(gpxTrack, ReduceSize.MEDIUM)
                println("Reduced #points for track ${gpxTrack.name} (${gpxTrack.trkseg.size}) to preview (${gpxTrackPreview.trkseg.size}) and detail (${gpxTrackDetail.trkseg.size})")

                val weather = weatherService.getWeather(
                    lat = gpxTrack.trkseg.first().lat,
                    lng = gpxTrack.trkseg.first().lon,
                    timestamp = trackTimestamp
                )
                val entity =
                    TrackEntity(
                        trackId = trackId(trackTimestamp),
                        trackName = gpxTrack.name,
                        dropboxId = dropboxTrack.id,
                        trackTimestamp = trackTimestamp,
                        totalTimerTime = metadata.totalTimerTime,
                        totalElapsedTime = metadata.totalElapsedTime,
                        totalAscent = metadata.totalAscent,
                        totalDescent = metadata.totalDescent,
                        totalCalories = metadata.totalCalories,
                        totalDistance = metadata.totalDistance,
                        gpxDataOriginal = gpxTrack,
                        gpxDataPreview = gpxTrackPreview,
                        gpxDataDetail = gpxTrackDetail,
                        weather = Weather(temperature = weather.temperature, weatherSymbol = weather.weatherSymbol()),
                    )

                println("Persisting new track id:${entity.trackId} name:${entity.trackName} timestamp:${entity.trackTimestamp}")
                trackRepository.save(entity)
            }
        }

        println("Syncing tracks with Dropbox took ${elapsed.inWholeMilliseconds}ms")
    }

    // 2024-05-09 08:17 Neckarsteig, Etappe 3 und 4
    private fun String.removeTimestamp(): String {
        val regex = Regex("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}) (.*)")
        if (regex.containsMatchIn(this)) {
            return regex.find(this)!!.groupValues[2]
        }
        return this
    }

    private fun String.removeExtension(): String {
        val idx = lastIndexOf(".")
        return if (idx > 0) {
            substring(0, idx)
        } else {
            this
        }
    }

    private fun trackId(trackTimestamp: Instant): String {
        return DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").withZone(ZoneId.of("Z"))
            .format(trackTimestamp)
    }
}