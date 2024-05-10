package de.sroeck.tracksbackend.gpxreduce

import de.sroeck.tracksbackend.fit2gpx.GpxTrk
import org.springframework.stereotype.Component

enum class ReduceSize(val epsilon: Double) {
    SMALL(0.00005), MEDIUM(0.00001), LARGE(0.000001);

}

@Component
class GpxReduceService {

    fun reduceGpx(gpxTrack: GpxTrk, reduceSize: ReduceSize): GpxTrk {
        val points = gpxTrack.trkseg.map { point -> Point(point.lat, point.lon, point) }
        val reducedPoints = douglasPeucker(points, reduceSize.epsilon).map { it.source }
        return GpxTrk(gpxTrack.name, gpxTrack.desc, reducedPoints)
    }

}