package de.sroeck.tracksbackend

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class TrackRestControllerTest {

    @Mock
    private lateinit var trackService: TrackService

    @InjectMocks
    private lateinit var trackRestController: TrackRestController

    @Test
    fun `get tracks and calculate bounds`() {
        val track1 = createTrack(Bounds(minLat = 1.0, maxLat = 5.0, minLon = 1.0, maxLon = 7.0))
        val track2 = createTrack(Bounds(minLat = 1.0, maxLat = 6.0, minLon = 2.0, maxLon = 3.0))
        `when`(trackService.listAll()).thenReturn(listOf(track1, track2))

        val mockMvc = MockMvcBuilders.standaloneSetup(trackRestController).build()

        val response = mockMvc
            .get("/tracks") {
                accept(MediaType.APPLICATION_JSON)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn()
        val responseDto = jacksonObjectMapper().readValue<JsonNode>(response.response.contentAsString)
        val calculatedBounds = jacksonObjectMapper().convertValue<Bounds>(responseDto.get("allTrackBounds"))
        assertThat(calculatedBounds).isEqualTo(Bounds(1.0, 6.0, 1.0, 7.0))
    }

    @Test
    fun `get tracks for empty service result`() {
        `when`(trackService.listAll()).thenReturn(emptyList())

        val mockMvc = MockMvcBuilders.standaloneSetup(trackRestController).build()

        mockMvc
            .get("/tracks") {
                accept(MediaType.APPLICATION_JSON)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { string("""{"tracks":[],"allTrackBounds":null}""") }
            }
    }


    private fun createTrack(bounds: Bounds) = object : TrackMetaData {
        override val trackId: String = "1"
        override val trackName: String = "name"
        override val dropboxId: String = "id123"
        override val trackTimestamp: Instant = Instant.ofEpochMilli(2)
        override val bounds: Bounds = bounds
        override val totalElapsedTime: Float = 1.0f
        override val totalTimerTime: Float = 1.0f
        override val totalDistance: Float = 11.0f
        override val totalAscent: Int = 122
        override val totalDescent: Int = 144
        override val totalCalories: Int = 1234
        override val weather: Weather = Weather("21°C", "n/a")
    }

}

