package de.sroeck.tracksbackend.fit2gpx

import com.garmin.fit.DateTime
import com.garmin.fit.Sport
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(fitDataPoints).hasSize(22045)

        assertThat(fitDataPoints[0].lat).isEqualTo(579288959)
        assertThat(fitDataPoints[0].lon).isEqualTo(104129988)
        assertThat(fitDataPoints[0].timestamp.equals(DateTime(Date.from(Instant.parse("2023-12-17T08:28:40Z")))))
        assertThat(fitDataPoints[0].altitude).isNull()
        assertThat(fitDataPoints[0].heartRate).isEqualTo(112)

        assertThat(fitSession.sport).isEqualTo(Sport.WALKING)
        assertThat(fitSession.startTime.equals(DateTime(Date.from(Instant.parse("2023-12-17T08:28:39Z")))))
        assertThat(fitSession.totalAscent).isEqualTo(886)
        assertThat(fitSession.totalDescent).isEqualTo(620)
        assertThat(fitSession.totalCalories).isEqualTo(1593)
        assertThat(fitSession.totalDistance).isEqualTo(35148.23f)
        assertThat(fitSession.totalElapsedTime).isEqualTo(23921.51f)
        assertThat(fitSession.totalTimerTime).isEqualTo(22558.66f)
    }

    @Test
    fun convertToGpx() {
        val fitData = parseFitFile(sampleFitFileAsBytes())

        val gpxData = convertFitToGpx(fitData)
        val gpxXmlString = convertGpxToString(gpxData)

        assertThat(gpxData.name).isEqualTo("Walking")
        assertThat(gpxData.desc).isEqualTo("name=Walking time=2023-12-17T08:28:39Z")
        assertThat(gpxXmlString).startsWith("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")

        assertThat(gpxXmlString).isEqualTo(sampleGpxFileAsString())
    }
}