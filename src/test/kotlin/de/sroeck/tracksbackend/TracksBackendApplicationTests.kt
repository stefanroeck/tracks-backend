package de.sroeck.tracksbackend

import com.github.tomakehurst.wiremock.client.WireMock.*
import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import de.sroeck.tracksbackend.fit2gpx.GpxTrkPt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.time.Instant


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(ConfigureWireMock(baseUrlProperties = ["dropbox.apiEndpoint", "dropbox.contentEndpoint"]))
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TracksBackendApplicationTests {

    companion object {
        const val accessToken: String = "0xABDEF"
    }

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun contextLoads() {
    }

    @Test
    fun `persist, load all and delete a gpx track`() {
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
            trackId = "abc",
            trackName = "Track One",
            dropboxId = "0xabc",
            trackTimestamp = Instant.now(),
            gpxDataOriginal = gpxTrackFull,
            gpxDataDetail = gpxTrackFull,
            gpxDataPreview = gpxTrackShort,
            totalElapsedTime = 11.0f,
            totalTimerTime = 12.0f,
            totalDistance = 134.0f,
            totalAscent = 42,
            totalDescent = 43,
            totalCalories = 555,
        )

        val trackId = trackRepository.save(trackEntity).trackId

        val savedEntity = webTestClient.get()
            .uri("/tracks/$trackId")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(TrackEntity::class.java)
            .returnResult().responseBody!!

        assertThat(savedEntity).usingRecursiveComparison()
            .comparingOnlyFields(
                "trackName",
                "dropboxId",
                "trackId",
                "totalElapsedTime",
                "totalElapsedTime",
                "totalTimerTime",
                "totalDistance",
                "totalAscent",
                "totalDescent",
                "totalCalories",
            )
            .isEqualTo(trackEntity)
        assertThat(savedEntity.internalTrackId).isGreaterThan(0)
        assertThat(savedEntity.gpxDataOriginalXml).isNull()
        assertThat(savedEntity.gpxDataPreviewXml).isNull()
        assertThat(savedEntity.gpxDataDetailXml).isNull()

        webTestClient.get()
            .uri("/tracks")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.allTrackBounds")
            .isEqualTo(
                mapOf("minLat" to 0.0, "maxLat" to 89.0, "minLon" to 0.0, "maxLon" to 179.0)
            ).jsonPath("$.tracks[0].trackId")
            .isEqualTo(trackEntity.trackId)
            .jsonPath("$.tracks.length()")
            .isEqualTo(1)


        webTestClient.delete()
            .uri("/tracks")
            .exchange()
            .expectStatus().is2xxSuccessful

        assertFetchAllTracksIsEmpty()
    }

    @Test
    fun `sync tracks via dropbox`() {
        stubOAuthRequest()
        stubSearchRequest()
        stubDownloadRequest()

        assertFetchAllTracksIsEmpty()

        webTestClient.post()
            .uri("/tracks/sync")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
            .isEmpty

        webTestClient.get()
            .uri("/tracks")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.tracks.length()")
            .isEqualTo(1)
            .jsonPath("$.tracks[0].trackId")
            .isEqualTo("20231217_0828")
            .jsonPath("$.tracks[0].trackName")
            .isEqualTo("track")
            .jsonPath("$.tracks[0].dropboxId")
            .isEqualTo("1")
            .jsonPath("$.tracks[0].bounds.minLat")
            .isEqualTo("48.46041667275131")
            .jsonPath("$.tracks[0].bounds.maxLat")
            .isEqualTo("48.555816896259785")
            .jsonPath("$.tracks[0].bounds.minLon")
            .isEqualTo("8.422242673113942")
            .jsonPath("$.tracks[0].bounds.maxLon")
            .isEqualTo("8.72803227044642")


        webTestClient.get()
            .uri("/tracks/20231217_0828/gpx")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
            .xpath("//trk/desc")
            .isEqualTo("activity=Walking name=track time=2023-12-17T08:28:39Z")
            .xpath("count(//trkpt)")
            .isEqualTo("437")

        webTestClient.get()
            .uri("/tracks/20231217_0828/gpx_detail")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
            .xpath("//trk/desc")
            .isEqualTo("activity=Walking name=track time=2023-12-17T08:28:39Z")
            .xpath("count(//trkpt)")
            .isEqualTo("1516")
    }

    private fun assertFetchAllTracksIsEmpty() {
        webTestClient.get()
            .uri("/tracks")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.tracks")
            .isEmpty
    }

    private fun stubOAuthRequest() {
        stubFor(
            post("/oauth2/token").willReturn(
                aResponse().withBody(
                    """
                            {
                                "access_token": "$accessToken",
                                "expires_in": 3600
                            }
                        """.trimIndent()
                )
            )
        )
    }

    private fun stubSearchRequest() {
        stubFor(
            post("/2/files/search_v2")
                .withHeader("Authorization", matching("Bearer $accessToken"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                                "matches": [
                                    {
                                        "metadata": {
                                            "metadata": {
                                                "id": "1",
                                                "path_display": "/path/track.fit",
                                                "name": "track.fit",
                                                "size": 100
                                            }                                    
                                        }
                                    }
                                ]
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    private fun stubDownloadRequest() {
        stubFor(
            post("/2/files/download")
                .withHeader("Authorization", matching("Bearer $accessToken"))
                .withHeader("Dropbox-API-Arg", matching("""\{"path":"/path/track.fit"}"""))
                .willReturn(
                    aResponse().withBodyFile("dropboxTrack.fit")
                )
        )
    }

}
