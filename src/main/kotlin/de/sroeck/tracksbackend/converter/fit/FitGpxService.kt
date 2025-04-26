package de.sroeck.tracksbackend.converter.fit

import de.sroeck.tracksbackend.converter.shared.*
import org.springframework.stereotype.Service

@Service
class FitGpxService : GpxConverter {

    fun parseAsFit(byteArray: ByteArray): FitData {
        return parseFitFile(byteArray)
    }

    fun convertToGpx(fitData: FitData, activityName: String): GpxTrk {
        return convertFitToGpx(fitData, activityName)
    }

    override fun convert(bytes: ByteArray, conversionContext: ConversionContext): ConversionResult {
        val fit = parseAsFit(bytes)
        val trackMetaData = with(fit.fitSession) {
            TrackMetaData(
                trackTimestamp = trackTimestamp(),
                totalElapsedTime = totalElapsedTime,
                totalTimerTime = totalTimerTime,
                totalDistance = totalDistance,
                totalAscent = totalAscent,
                totalDescent = totalDescent,
                totalCalories = totalCalories,
            )
        }
        return ConversionResult(
            gpx = convertToGpx(fit, conversionContext.activityName),
            metaData = trackMetaData,
        )
    }

    override fun canHandle(discriminator: String): Boolean {
        return discriminator.endsWith(".fit")
    }
}