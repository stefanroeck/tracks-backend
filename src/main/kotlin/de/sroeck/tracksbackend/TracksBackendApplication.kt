package de.sroeck.tracksbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@PropertySource("classpath:application.properties", "classpath:secrets.properties")
class TracksBackendApplication

fun main(args: Array<String>) {
    runApplication<TracksBackendApplication>(*args)
}
