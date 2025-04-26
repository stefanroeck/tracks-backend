package de.sroeck.tracksbackend.converter.tcx

import de.sroeck.tracksbackend.converter.shared.GpxTrkPt
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class TcxGpxServiceTest {

    private val tcxGpxService = TcxGpxService()

    @Test
    fun `parse sample tcx file`() {
        val result = tcxGpxService.parseTcxBytes(sampleTcxFileAsBytes())

        Assertions.assertThat(result.activities).hasSize(1)
        with<Activity, Any>(result.activities.first()) {
            Assertions.assertThat(this.id).isEqualTo("2022-09-16T07:26:21+02:00")
            Assertions.assertThat(this.sport).isEqualTo("Biking")
            Assertions.assertThat(this.laps).hasSize(2)

            with(this.laps.first()) {
                Assertions.assertThat(this.startTime).isEqualTo("2022-09-16T07:26:21+02:00")
                Assertions.assertThat(this.distanceMeters).isEqualTo(5001.82)
                Assertions.assertThat(this.totalTimeSeconds).isEqualTo(793.0)
                Assertions.assertThat(this.trackpoints).hasSize(794)

                with(this.trackpoints.first()) {
                    Assertions.assertThat(this.position).isEqualTo(
                        Position(
                            latitudeDegrees = 49.02793,
                            longitudeDegrees = 8.783435
                        )
                    )
                    Assertions.assertThat(this.time).isEqualTo("2022-09-16T07:26:21+02:00")
                    Assertions.assertThat(this.altitudeMeters).isEqualTo(215.0)
                }
            }
        }
    }

    @Test
    fun `parse tcx and convert to gpx`() {
        val trainingCenterDatabase = tcxGpxService.parseTcxBytes(sampleTcxFileAsBytes())
        val result = tcxGpxService.tcxToGpx(trainingCenterDatabase, "Biketour 123")
        val expectedPointCount =
            trainingCenterDatabase.activities.flatMap { it.laps }.flatMap { it.trackpoints }.count()

        Assertions.assertThat(expectedPointCount).isEqualTo(1206)
        Assertions.assertThat(result.trkseg).hasSize(expectedPointCount)
        Assertions.assertThat(result.trkseg.first()).isEqualTo(
            GpxTrkPt(
                lat = 49.02793,
                lon = 8.783435,
                ele = 215,
                time = "2022-09-16T07:26:21+02:00"
            )
        )
        Assertions.assertThat(result.name).isEqualTo("Biketour 123")
        Assertions.assertThat(result.desc).isEqualTo("activity=Biking name=Biketour 123 time=2022-09-16T07:26:21+02:00")
    }


    private fun sampleTcxFileAsBytes(): ByteArray {
        val bytes = TcxGpxServiceTest::class.java.classLoader.getResource("sampleTcxFile.tcx")?.readBytes()
            ?: throw IllegalStateException("Cannot read sampleTcxFile.tcx")
        return bytes
    }

}