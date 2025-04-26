package de.sroeck.tracksbackend.converter.shared

data class ConversionResult(val gpx: GpxTrk, val metaData: TrackMetaData)

data class ConversionContext(val activityName: String)

interface GpxConverter {
    fun convert(bytes: ByteArray, conversionContext: ConversionContext): ConversionResult
    fun canHandle(discriminator: String): Boolean
}