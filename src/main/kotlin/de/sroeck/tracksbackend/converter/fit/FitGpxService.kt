package de.sroeck.tracksbackend.converter.fit

import de.sroeck.tracksbackend.converter.shared.GpxTrk
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