package de.sroeck.tracksbackend.weather

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant

@Service
class WeatherService(
    @Autowired private val httpClient: HttpClient,
    @Autowired private val objectMapper: ObjectMapper,
) {

    fun getWeather(lat: Double, lng: Double, timestamp: Instant): WeatherResult {
        // eg: 2022-03-14
        val day: String = timestamp.toString().substring(0, 10)
        println("Fetching weather data: $lat, $lng, $day")
        val urlStr =
            "https://archive-api.open-meteo.com/v1/era5?latitude=$lat&longitude=$lng&start_date=$day&end_date=$day&daily=weathercode,temperature_2m_max&timezone=CET"

        val request = HttpRequest.newBuilder()
            .uri(URI(urlStr))
            .headers("Content-Type", "application/json")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        return if (response.statusCode() != 200) {
            println("Unexpected status when fetching weather data: ${response.statusCode()}")
            WeatherResult(lat, lng, day, "n/a", 0)
        } else {
            val json = objectMapper.readValue(response.body(), JsonNode::class.java)

            val daily = json.get("daily")
            val dailyUnits = json.get("daily_units")
            val temperature =
                daily.get("temperature_2m_max").firstNotNullOfOrNull { it.asDouble() } ?: Double.NaN
            val temperatureUnit = dailyUnits.get("temperature_2m_max").asText()

            val result = WeatherResult(
                lat = lat,
                lng = lng,
                day = day,
                temperature = if (!temperature.isNaN()) "$temperature$temperatureUnit" else "",
                weatherCode = daily.get("weathercode").firstNotNullOfOrNull { it.asInt() } ?: 0
            )
            result
        }
    }

}

data class WeatherResult(
    val lat: Double,
    val lng: Double,
    val day: String,
    val temperature: String,
    val weatherCode: Int,
) {
    fun weatherSymbol() = weatherCodeToSymbol(weatherCode)
}

fun weatherCodeToSymbol(weatherCode: Int): String {
    return when (weatherCode) {
        0, 1, 2, 3 -> "🌤"
        45, 48, 51, 53, 55 -> "🌥"
        61, 63, 65, 66, 67 -> "🌧"
        71, 73, 75, 77, 85, 86 -> "🌨"
        95, 96, 99 -> "🌩"
        else -> {
            println("Unknown weatherCode: $weatherCode")
            "?"
        }
    }
}
