package de.sroeck.tracksbackend.converter.shared

import java.time.Instant

data class TrackMetaData(
    val trackTimestamp: Instant,
    val totalElapsedTime: Float,
    val totalTimerTime: Float,
    val totalDistance: Float,
    val totalAscent: Int,
    val totalDescent: Int,
    val totalCalories: Int,
)
