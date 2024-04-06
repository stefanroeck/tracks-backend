package de.sroeck.tracksbackend.fit

import com.garmin.fit.*
import java.io.ByteArrayInputStream

data class FitDataPoint(val lat: Int, val lon: Int, val timestamp: DateTime, val altitude: Float?, val heartRate: Short?)
data class FitData (val fitDataPoints: List<FitDataPoint>)
fun parseFitFile(fitFile: ByteArray): FitData {
    val decoder = Decode()
    val collectedDataPoints = ArrayList<FitDataPoint>();

    if (!decoder.checkFileIntegrity(ByteArrayInputStream(fitFile))) {
        error("provided file is not a valid fit file")
    }
    val msgBroadcaster = MesgBroadcaster(decoder)
    msgBroadcaster.addListener(RecordMesgListener { message ->
        if (message != null && message.positionLat != null && message.positionLong != null && message.timestamp != null) {
            collectedDataPoints.add(FitDataPoint(message.positionLat, message.positionLong, message.timestamp, message.altitude, message.heartRate))
        }
        //println(message?.fields?.map { t -> t.name })
        //println("${message?.timestamp}: ${message?.positionLat}:${message?.positionLong}, alt: ${message?.altitude}, heart: ${message?.heartRate}")
    })
    msgBroadcaster.addListener(ActivityMesgListener { message ->
        println(message?.fields?.map { t -> t.name })
        println("${message?.type} - ${message?.totalTimerTime} - ${message?.numSessions} : ${message?.eventType}")
    })

    decoder.read(ByteArrayInputStream(fitFile), msgBroadcaster)
    return FitData(collectedDataPoints)
}