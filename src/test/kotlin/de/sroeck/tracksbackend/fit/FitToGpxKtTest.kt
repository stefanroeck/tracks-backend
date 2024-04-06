package de.sroeck.tracksbackend.fit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FitToGpxKtTest {

    @Test
    fun parseFitFile() {
        val bytes = FitToGpxKtTest::class.java.classLoader.getResource("sampleFitFile.fit")?.readBytes()

        assertThat(bytes).isNotNull()

        bytes?.let {
            val res = parseFitFile(it)
            assertThat(res.fitDataPoints).hasSize(22045)

            assertThat(res.fitDataPoints[0].lat).isEqualTo(579288959)
            assertThat(res.fitDataPoints[0].lon).isEqualTo(104129988)
            assertThat(res.fitDataPoints[0].timestamp.toString()).isEqualTo("Sun Dec 17 09:28:40 CET 2023")
            assertThat(res.fitDataPoints[0].altitude).isNull()
            assertThat(res.fitDataPoints[0].heartRate).isEqualTo(112)
        }

    }
}