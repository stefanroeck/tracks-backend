import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

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