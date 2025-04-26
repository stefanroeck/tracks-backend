package de.sroeck.tracksbackend.tcx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import de.sroeck.tracksbackend.fit2gpx.GpxTrkPt
import org.springframework.stereotype.Service

@Service
class TcxGpxService {

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
        return GpxTrk(name = activityName, desc = trainingCenterDatabase.activities.first().id ?: "", trkseg = points)

    }
}