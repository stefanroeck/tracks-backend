package de.sroeck.tracksbackend.converter.fit

import com.garmin.fit.DateTime
import com.garmin.fit.Sport
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class ParseFitKtTest {

    private fun sampleFitFileAsBytes(): ByteArray {
        val bytes = ParseFitKtTest::class.java.classLoader.getResource("sampleFitFile.fit")?.readBytes()
            ?: throw IllegalStateException("Cannot read sampleFitFile.fit")
        return bytes
    }

    private fun sampleGpxFileAsString(): String {
        val text = ParseFitKtTest::class.java.classLoader.getResource("sampleGpxFile.xml")?.readText(Charsets.UTF_8)
            ?: throw IllegalStateException("Cannot read sampleGpxFile.xml")
        return text
    }

    @Test
    fun parseFitFile() {
        val res = parseFitFile(sampleFitFileAsBytes())
        val (fitDataPoints, fitSession) = res
        Assertions.assertThat(fitDataPoints).hasSize(22045)

        Assertions.assertThat(fitDataPoints[0].lat).isEqualTo(579288959)
        Assertions.assertThat(fitDataPoints[0].lon).isEqualTo(104129988)
        Assertions.assertThat(fitDataPoints[0].timestamp.equals(DateTime(Date.from(Instant.parse("2023-12-17T08:28:40Z")))))
        Assertions.assertThat(fitDataPoints[0].altitude).isNull()
        Assertions.assertThat(fitDataPoints[0].heartRate).isEqualTo(112)

        Assertions.assertThat(fitSession.sport).isEqualTo(Sport.WALKING)
        Assertions.assertThat(fitSession.startTime.equals(DateTime(Date.from(Instant.parse("2023-12-17T08:28:39Z")))))
        Assertions.assertThat(fitSession.totalAscent).isEqualTo(886)
        Assertions.assertThat(fitSession.totalDescent).isEqualTo(620)
        Assertions.assertThat(fitSession.totalCalories).isEqualTo(1593)
        Assertions.assertThat(fitSession.totalDistance).isEqualTo(35148.23f)
        Assertions.assertThat(fitSession.totalElapsedTime).isEqualTo(23921.51f)
        Assertions.assertThat(fitSession.totalTimerTime).isEqualTo(22558.66f)
    }

    @Test
    fun convertToGpx() {
        val fitData = parseFitFile(sampleFitFileAsBytes())

        val gpxData = convertFitToGpx(fitData, "Walk in the park")
        val gpxXmlString = convertGpxToString(gpxData)

        Assertions.assertThat(gpxData.name).isEqualTo("Walk in the park")
        Assertions.assertThat(gpxData.desc)
            .isEqualTo("activity=Walking name=Walk in the park time=2023-12-17T08:28:39Z")
        Assertions.assertThat(gpxXmlString).startsWith("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")

        Assertions.assertThat(gpxXmlString).isEqualTo(sampleGpxFileAsString())
    }
}