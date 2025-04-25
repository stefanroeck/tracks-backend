package de.sroeck.tracksbackend.tcx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Service

@Service
class TcxGpxService {

    fun parseTcxBytes(bytes: ByteArray): TrainingCenterDatabase {
        val xmlMapper = XmlMapper().registerKotlinModule().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return xmlMapper.readValue(bytes, TrainingCenterDatabase::class.java)
    }
}