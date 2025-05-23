package de.sroeck.tracksbackend.converter.shared

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/** Data class representing a GPX track, according to gpx.xsd, see https://www.topografix.com/gpx.asp */
@JacksonXmlRootElement(localName = "trk")
data class GpxTrk(
    val name: String,
    val desc: String,
    @field:JacksonXmlElementWrapper(useWrapping = true, localName = "trkseg")
    @field:JacksonXmlProperty(localName = "trkpt") val trkseg: List<GpxTrkPt>
)

data class GpxTrkPt(
    @field:JacksonXmlProperty(isAttribute = true) val lat: Double,
    @field:JacksonXmlProperty(isAttribute = true) val lon: Double,
    val ele: Int?,
    val time: String
)