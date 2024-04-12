package de.sroeck.tracksbackend

import GpxTrk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TrackService (@Autowired private val trackRepository: TrackRepository){

    fun persist() {
        trackRepository.save(GpxTrk("testTrack" + System.currentTimeMillis(), listOf()))
    }

    fun listAll(): List<GpxTrk> {
        return trackRepository.findAll().toList()
    }

}