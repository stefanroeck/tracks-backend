import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.garmin.fit.DateTime
import de.sroeck.tracksbackend.fit2gpx.FitData
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.pow

@JacksonXmlRootElement(localName = "trk")
private data class GpxTrk(
    val name: String,
    @field:JacksonXmlElementWrapper(useWrapping = true, localName = "trkseg")
    @field:JacksonXmlProperty(localName = "trkpt") val trkseg: List<GpxTrkPt>
)

private data class GpxTrkPt(
    @field:JacksonXmlProperty(isAttribute = true) val lat: Double,
    @field:JacksonXmlProperty(isAttribute = true) val lon: Double,
    val ele: Int?,
    val time: String
)

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
    return semicircles * ( 180 / 2.0.pow(31.0))
}

fun convertToGpx(fitData: FitData, name: String): String {
    val trkPoints = fitData.fitDataPoints.map { GpxTrkPt(semicirclesToDegrees(it.lat), semicirclesToDegrees(it.lon), it.altitude?.toInt(), formatFitDate(it.timestamp)) }

    val track = GpxTrk(name, trkPoints)

    val xmlMapper = XmlMapper()
    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    return GPX_HEADER + xmlMapper.writeValueAsString(track) + GPX_FOOTER
}