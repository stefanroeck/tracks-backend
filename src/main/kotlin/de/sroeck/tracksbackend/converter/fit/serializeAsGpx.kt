package de.sroeck.tracksbackend.converter.fit

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.garmin.fit.DateTime
import de.sroeck.tracksbackend.converter.shared.GpxTrk
import de.sroeck.tracksbackend.converter.shared.GpxTrkPt
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.pow

private val GPX_HEADER = """
<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd" version="1.1"
     creator="gpx-backend">
""".trimIndent() + "\n"

private const val GPX_FOOTER = "</gpx>"

private fun formatFitDate(datetime: DateTime): String {
    return DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(ZoneId.of("Z")).format(datetime.date.toInstant())
}


private fun semicirclesToDegrees(semicircles: Int): Double {
    return semicircles * (180 / 2.0.pow(31.0))
}

fun convertFitToGpx(fitData: FitData, activityName: String): GpxTrk {
    val trkPoints = fitData.fitDataPoints
        .filter { it.altitude !== null }
        .map {
            GpxTrkPt(
                semicirclesToDegrees(it.lat),
                semicirclesToDegrees(it.lon),
                it.altitude?.toInt(),
                formatFitDate(it.timestamp)
            )
        }
    val sport = titleCase(fitData.fitSession.sport.name)
    val description = "activity=${sport} name=${activityName} time=${formatFitDate(fitData.fitSession.startTime)}"
    return GpxTrk(activityName, description, trkPoints)
}

fun titleCase(name: String): String {
    return name.first().uppercase() + name.substring(1).lowercase()
}

fun convertGpxToString(gpxTrk: GpxTrk): String {
    val xmlMapper = XmlMapper()
    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT)
    return GPX_HEADER + xmlMapper.writeValueAsString(gpxTrk) + GPX_FOOTER
}