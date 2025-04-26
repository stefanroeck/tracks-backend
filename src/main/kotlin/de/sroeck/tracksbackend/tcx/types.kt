package de.sroeck.tracksbackend.tcx

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "TrainingCenterDatabase")
data class TrainingCenterDatabase(
    @JacksonXmlProperty(localName = "Activities")
    val activities: List<Activity>

)

data class Activity(
    @JacksonXmlProperty(isAttribute = true, localName = "Sport")
    val sport: String? = null,

    @JacksonXmlProperty(localName = "Id")
    val id: String? = null,

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Lap")
    val laps: List<Lap> = emptyList()
)

data class Lap(
    @JacksonXmlProperty(isAttribute = true, localName = "StartTime")
    val startTime: String? = null,

    @JacksonXmlProperty(localName = "TotalTimeSeconds")
    val totalTimeSeconds: Double? = null,

    @JacksonXmlProperty(localName = "DistanceMeters")
    val distanceMeters: Double? = null,

    @JacksonXmlProperty(localName = "Calories")
    val calories: Int? = null,

    @JacksonXmlProperty(localName = "Track")
    val trackpoints: List<Trackpoint> = emptyList()
)

data class Trackpoint(
    @JacksonXmlProperty(localName = "Time")
    val time: String? = null,

    @JacksonXmlProperty(localName = "Position")
    val position: Position? = null,

    @JacksonXmlProperty(localName = "AltitudeMeters")
    val altitudeMeters: Double? = null,

    @JacksonXmlProperty(localName = "DistanceMeters")
    val distanceMeters: Double? = null,

    @JacksonXmlProperty(localName = "HeartRateBpm")
    val heartRateBpm: HeartRateBpm? = null
)

data class Position(
    @JacksonXmlProperty(localName = "LatitudeDegrees")
    val latitudeDegrees: Double? = null,

    @JacksonXmlProperty(localName = "LongitudeDegrees")
    val longitudeDegrees: Double? = null
)

data class HeartRateBpm(
    @JacksonXmlProperty(localName = "Value")
    val value: Int? = null
)
