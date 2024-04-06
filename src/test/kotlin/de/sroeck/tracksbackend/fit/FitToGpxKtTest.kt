package de.sroeck.tracksbackend.fit

import com.garmin.fit.Sport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FitToGpxKtTest {

    @Test
    fun parseFitFile() {
        val bytes = FitToGpxKtTest::class.java.classLoader.getResource("sampleFitFile.fit")?.readBytes()

        assertThat(bytes).isNotNull()

        bytes?.let {
            val res = parseFitFile(it)
            val ( fitDataPoints, fitSession ) = res;
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

    }
}