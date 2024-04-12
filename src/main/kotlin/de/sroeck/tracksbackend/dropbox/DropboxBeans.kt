package de.sroeck.tracksbackend.dropbox

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class DropboxBeans {
    @Bean("DropboxHttpClient")
    fun httpClient(): HttpClient {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build()
    }

    @Component
    class DropboxConnectionParams (
            @Value("\${dropbox.clientId}") val clientId: String,
            @Value("\${dropbox.secret}") val secret: String,
            @Value("\${dropbox.refreshToken}") val refreshToken: String,
    )

}
