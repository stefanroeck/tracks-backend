package de.sroeck.tracksbackend

import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import de.sroeck.tracksbackend.fit2gpx.GpxTrkPt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class TracksBackendApplicationTests {

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Test
    fun contextLoads() {
    }

    @Test
    fun `persist, load and delete a single gpx track`() {
        val trackSegments = (0..10_000).map {
            GpxTrkPt(
                lat = (it % 90).toDouble(),
                lon = (it % 180).toDouble(),
                ele = 40,
                time = "2020-01-01"
            )
        }
        val gpxTrackFull = GpxTrk(name = "gpx1", desc = "empty", trkseg = trackSegments)
        val gpxTrackShort = GpxTrk(name = "gpx1", desc = "empty", trkseg = trackSegments.subList(0, 255))
        val trackEntity = TrackEntity(
            trackId = "1",
            trackName = "Track One",
            dropboxId = "0xabc",
            trackTimestamp = Instant.now(),
            gpxDataOriginal = gpxTrackFull,
            gpxDataDetail = gpxTrackFull,
            gpxDataPreview = gpxTrackShort,
        )

        val trackId = trackRepository.save(trackEntity).trackId

        val savedEntity = trackRepository.findById(trackId).get()
        assertThat(savedEntity).usingRecursiveComparison().ignoringFields("internalTrackId", "trackTimestamp")
            .isEqualTo(trackEntity)
        assertThat(savedEntity.internalTrackId).isGreaterThan(0)

        val allEntities = trackRepository.findAll()
        assertThat(allEntities).hasSize(1).allMatch { it.internalTrackId == savedEntity.internalTrackId }

        trackRepository.delete(savedEntity)
        assertThat(trackRepository.findAll()).isEmpty()
    }

}
