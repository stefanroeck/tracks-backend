package de.sroeck.tracksbackend.gpxreduce

import de.sroeck.tracksbackend.converter.fit.ParseFitKtTest
import de.sroeck.tracksbackend.converter.fit.convertFitToGpx
import de.sroeck.tracksbackend.converter.fit.parseFitFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GpxReduceServiceTest {

    private fun sampleFitFileAsBytes(): ByteArray {
        val bytes = ParseFitKtTest::class.java.classLoader.getResource("sampleFitFile.fit")?.readBytes()
            ?: throw IllegalStateException("Cannot read sampleFitFile.fit")
        return bytes
    }

    private val gpxReduceService = GpxReduceService()

    @Test
    fun reduceGpx() {
        val fitData = parseFitFile(sampleFitFileAsBytes())
        val gpxData = convertFitToGpx(fitData, "Dummy")

        assertThat(gpxData.trkseg.size).isEqualTo(22024)
        assertThat(gpxReduceService.reduceGpx(gpxData, ReduceSize.LARGE).trkseg.size).isEqualTo(6193)
        assertThat(gpxReduceService.reduceGpx(gpxData, ReduceSize.MEDIUM).trkseg.size).isEqualTo(1516)
        assertThat(gpxReduceService.reduceGpx(gpxData, ReduceSize.SMALL).trkseg.size).isEqualTo(437)
    }
}