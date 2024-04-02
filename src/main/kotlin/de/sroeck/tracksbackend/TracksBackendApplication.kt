package de.sroeck.tracksbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TracksBackendApplication

fun main(args: Array<String>) {
	runApplication<TracksBackendApplication>(*args)
}
