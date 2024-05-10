package de.sroeck.tracksbackend.gpxreduce

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class GpxreduceKtTest {

    @Test
    fun douglasPeucker_straightLine() {
        val result = douglasPeucker(
            listOf(
                Point(1.0, 1.0, "A"),
                Point(1.0, 2.0, "B"),
                Point(1.0, 3.0, "C")
            ), 0.1
        )

        assertThat(result).hasSize(2).containsExactly(
            Point(1.0, 1.0, "A"),
            Point(1.0, 3.0, "C"),
        )
    }

    @Test
    fun douglasPeucker_curve() {
        val result = douglasPeucker(
            listOf(
                Point(-1.0, 0.0, "A"),
                Point(0.0, 2.0, "B"),
                Point(1.0, 0.0, "C"),
            ), 0.1
        )

        assertThat(result).hasSize(3).containsExactly(
            Point(-1.0, 0.0, "A"),
            Point(0.0, 2.0, "B"),
            Point(1.0, 0.0, "C"),
        )
    }

    @TestFactory
    fun douglasPeucker_noops() = listOf(
        listOf(),
        listOf(
            Point(1.0, 1.0, "A"),
        ),
        listOf(
            Point(-1.0, 0.0, "A"),
            Point(0.0, 2.0, "B"),
        ),
    ).map { points ->
        DynamicTest.dynamicTest("douglasPeucker_noops $points") {
            val result = douglasPeucker(points, 0.1)

            assertThat(result).isEqualTo(points)
        }
    }

}