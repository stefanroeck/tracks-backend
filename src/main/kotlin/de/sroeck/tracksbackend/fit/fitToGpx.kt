package de.sroeck.tracksbackend.fit

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.garmin.fit.*
import java.io.ByteArrayInputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.pow

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


@JacksonXmlRootElement(localName = "trk")
data class GpxTrk(
    val name: String,
    @field:JacksonXmlElementWrapper(useWrapping = true, localName = "trkseg")
    @field:JacksonXmlProperty(localName = "trkpt") val trkseg: List<GpxTrkPt>
)

data class GpxTrkPt(
    @field:JacksonXmlProperty(isAttribute = true) val lat: Double,
    @field:JacksonXmlProperty(isAttribute = true) val lon: Double,
    val ele: Int?,
    val time: String
)

val GPX_HEADER = """
<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd" version="1.1"
     creator="gpx-backend">
""".trimIndent() + "\n"

const val GPX_FOOTER = "</gpx>"

fun formatFitDate(datetime: DateTime): String {
    return DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(ZoneId.of("Z")).format(datetime.date.toInstant())
}

fun semicirclesToDegrees(semicircles: Int): Double {
    return semicircles * ( 180 / 2.0.pow(31.0))
}

fun convertToGpx(fitData: FitData, name: String): String {
    val trkPoints = fitData.fitDataPoints.map { GpxTrkPt(semicirclesToDegrees(it.lat), semicirclesToDegrees(it.lon), it.altitude?.toInt(), formatFitDate(it.timestamp)) }

    val track = GpxTrk(name, trkPoints)

    val xmlMapper = XmlMapper()
    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    return GPX_HEADER + xmlMapper.writeValueAsString(track) + GPX_FOOTER
}