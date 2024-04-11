package de.sroeck.tracksbackend.fit

import com.garmin.fit.Sport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class FitToGpxKtTest {

    private fun sampleFitFileAsBytes(): ByteArray {
        val bytes = FitToGpxKtTest::class.java.classLoader.getResource("sampleFitFile.fit")?.readBytes()
            ?: throw IllegalStateException("Cannot read sampleFitFile.fit")
        return bytes
    }

    private fun sampleGpxFileAsString(): String {
        val text = FitToGpxKtTest::class.java.classLoader.getResource("sampleGpxFile.xml")?.readText(Charsets.UTF_8)
            ?: throw IllegalStateException("Cannot read sampleGpxFile.xml")
        return text
    }

    @Test
    fun parseFitFile() {
        val res = parseFitFile(sampleFitFileAsBytes())
        val (fitDataPoints, fitSession) = res;
        assertThat(fitDataPoints).hasSize(22045)

        assertThat(fitDataPoints[0].lat).isEqualTo(579288959)
        assertThat(fitDataPoints[0].lon).isEqualTo(104129988)
        assertThat(fitDataPoints[0].timestamp.toString()).isEqualTo("Sun Dec 17 09:28:40 CET 2023")
        assertThat(fitDataPoints[0].altitude).isNull()
        assertThat(fitDataPoints[0].heartRate).isEqualTo(112)

        assertThat(fitSession.sport).isEqualTo(Sport.WALKING)
        assertThat(fitSession.startTime.toString()).isEqualTo("Sun Dec 17 09:28:39 CET 2023")
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

        val gpxXmlString = convertToGpx(fitData, "Imported File")

        assertThat(gpxXmlString).startsWith("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")

        assertThat(gpxXmlString).isEqualTo(sampleGpxFileAsString());
    }
}