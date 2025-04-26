package de.sroeck.tracksbackend.converter.tcx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.sroeck.tracksbackend.converter.shared.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TcxGpxService : GpxConverter {

    fun parseTcxBytes(bytes: ByteArray): TrainingCenterDatabase {
        val xmlMapper = XmlMapper().registerKotlinModule().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return xmlMapper.readValue(bytes, TrainingCenterDatabase::class.java)
    }

    fun tcxToGpx(trainingCenterDatabase: TrainingCenterDatabase, activityName: String): GpxTrk {
        val points = trainingCenterDatabase.activities
            .flatMap { it.laps }
            .flatMap { it.trackpoints }
            .filter { it.position != null }
            .filter { it.time != null }
            .filter { it.altitudeMeters != null }
            .map {
                GpxTrkPt(
                    lat = it.position!!.latitudeDegrees!!,
                    lon = it.position.longitudeDegrees!!,
                    ele = it.altitudeMeters!!.toInt(),
                    time = it.time!!
                )
            }
        val activity = trainingCenterDatabase.activities.first()
        val description = "activity=${activity.sport} name=${activityName} time=${activity.id}"

        return GpxTrk(name = activityName, desc = description, trkseg = points)

    }

    override fun convert(
        bytes: ByteArray,
        conversionContext: ConversionContext
    ): ConversionResult {
        val centerDatabase = parseTcxBytes(bytes)
        val activity = centerDatabase.activities.first()
        val gpx = tcxToGpx(centerDatabase, conversionContext.activityName)
        val metaData = TrackMetaData(
            trackTimestamp = Instant.parse(activity.laps.first().startTime!!),
            totalElapsedTime = activity.laps.sumOf { it.totalTimeSeconds ?: 0.0 }.toFloat(),
            totalTimerTime = activity.laps.sumOf { it.totalTimeSeconds ?: 0.0 }.toFloat(),
            totalDistance = activity.laps.sumOf { it.distanceMeters ?: 0.0 }.toFloat(),
            totalAscent = 0, // TODO
            totalDescent = 0, // TODO
            totalCalories = activity.laps.sumOf { it.calories ?: 0 }
        )
        return ConversionResult(gpx = gpx, metaData = metaData)
    }

    override fun canHandle(discriminator: String): Boolean {
        return discriminator.endsWith(".tcx")
    }
}