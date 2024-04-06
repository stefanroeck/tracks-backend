package de.sroeck.tracksbackend.fit

import com.garmin.fit.*
import java.io.ByteArrayInputStream

data class FitDataPoint(
    val lat: Int,
    val lon: Int,
    val timestamp: DateTime,
    val altitude: Float?,
    val heartRate: Short?
)

data class FitSession(
    val startTime: DateTime,
    val sport: Sport,
    val totalElapsedTime: Float,
    val totalTimerTime: Float,
    val totalDistance: Float,
    val totalAscent: Int,
    val totalDescent: Int,
    val totalCalories: Int
)

data class FitData(val fitDataPoints: List<FitDataPoint>, val fitSession: FitSession)

fun parseFitFile(fitFile: ByteArray): FitData {
    val decoder = Decode()
    val collectedDataPoints = ArrayList<FitDataPoint>()

    if (!decoder.checkFileIntegrity(ByteArrayInputStream(fitFile))) {
        error("provided file is not a valid fit file")
    }
    val msgBroadcaster = MesgBroadcaster(decoder)
    msgBroadcaster.addListener(RecordMesgListener { message ->
        if (message.positionLat != null && message.positionLong != null && message.timestamp != null) {
            collectedDataPoints.add(FitDataPoint(message.positionLat, message.positionLong, message.timestamp, message.altitude, message.heartRate))
        }
    })

    var fitSession: FitSession? = null
    msgBroadcaster.addListener(SessionMesgListener { message ->
        fitSession = FitSession(message.startTime, message.sport, message.totalElapsedTime, message.totalTimerTime, message.totalDistance, message.totalAscent, message.totalDescent, message.totalCalories)
    })

    decoder.read(ByteArrayInputStream(fitFile), msgBroadcaster)

    if (fitSession == null) {
        error("No fitSession found in fit file")
    }

    return FitData(collectedDataPoints, fitSession!!)
}