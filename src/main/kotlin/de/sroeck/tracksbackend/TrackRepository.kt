package de.sroeck.tracksbackend

import GpxTrk
import org.springframework.data.repository.CrudRepository


interface TrackRepository : CrudRepository<GpxTrk, String> {
}