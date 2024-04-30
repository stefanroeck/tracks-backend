package de.sroeck.tracksbackend.fit2gpx

import org.springframework.stereotype.Service

@Service
class FitGpxService {

    fun parseAsFit(byteArray: ByteArray): FitData {
        return parseFitFile(byteArray)
    }

    fun convertToGpx(fitData: FitData, activityName: String): GpxTrk {
        return convertFitToGpx(fitData, activityName)
    }
}