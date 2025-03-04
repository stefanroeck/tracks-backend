package de.sroeck.tracksbackend.dropbox

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant

@Service
class DropboxApi(
    val objectMapper: ObjectMapper,
    val httpClient: HttpClient,
    val dropboxConnectionParams: DropboxBeans.DropboxConnectionParams
) {

    private class AccessToken(val accessToken: String, expiresIn: Long) {
        private val expiresAt = Instant.now().plusSeconds(expiresIn)

        fun expired() = Instant.now().isAfter(expiresAt)
    }

    private data class DropboxSearchRequestOptions(
        val path: String,
        @get:JsonProperty("file_extensions") val fileExtensions: List<String>
    )


    private data class DropboxSearchRequest(val query: String, val options: DropboxSearchRequestOptions)

    data class DropboxTracks(val id: String, val path: String, val name: String, val size: Int)

    private var accessToken: AccessToken? = null

    fun fetchTracks(): List<DropboxTracks> {

        val params = DropboxSearchRequest(
            "#longdistancewalk",
            DropboxSearchRequestOptions("/Apps/Runalyze/activities", listOf("fit"))
        )
        val queryParam = objectMapper.writeValueAsString(params)
        println("Querying all tracks with: $queryParam")
        val request = HttpRequest.newBuilder()
            .uri(URI("${dropboxConnectionParams.apiEndpoint}/2/files/search_v2"))
            .headers("Content-Type", "application/json")
            .headers("Authorization", "Bearer ${getAccessToken()}")
            .POST(HttpRequest.BodyPublishers.ofString(queryParam))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val jsonResponse = objectMapper.readValue(response.body(), JsonNode::class.java)
            val matches = jsonResponse.get("matches")
            return matches
                .map { match: JsonNode -> { match.get("metadata").get("metadata") } }
                .map { file: () -> JsonNode ->
                    DropboxTracks(
                        file().get("id").asText(),
                        file().get("path_display").asText(),
                        file().get("name").asText(),
                        file().get("size").asInt()
                    )
                }
        } else {
            println("Unexpected dropbox response: code=${response.statusCode()}, body=${response.body()}")
            return emptyList()
        }
    }

    fun downloadTrack(path: String): ByteArray {
        println("Downloading track: $path")
        data class DropboxApiArg(val path: String)

        val request = HttpRequest.newBuilder()
            .uri(URI("${dropboxConnectionParams.contentEndpoint}/2/files/download"))
            .headers("Content-Type", "text/plain")
            .headers("Authorization", "Bearer ${getAccessToken()}")
            .headers("Dropbox-API-Arg", objectMapper.writeValueAsString(DropboxApiArg(path)))
            .POST(HttpRequest.BodyPublishers.ofString(""))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() == 200) {
            // response is the file
            return response.body()
        } else {
            error("Unexpected dropbox response: code=${response.statusCode()}, body=${String(response.body())}")
        }
    }

    private fun toFormData(map: Map<String, String>): String {
        return map.map { (key, value) -> "$key=$value" }.joinToString("&")
    }

    private data class AccessTokenResponse(
        @get:JsonProperty("access_token") val accessToken: String,
        @get:JsonProperty("expires_in") val expiresIn: Long
    )

    private fun getAccessToken(): String {
        val existingToken = this.accessToken?.takeIf { t -> !t.expired() }
        if (existingToken != null) {
            return existingToken.accessToken
        }

        println("Obtaining new accessToken")
        val accessTokenResponse = fetchAccessToken()
        this.accessToken = AccessToken(accessTokenResponse.accessToken, accessTokenResponse.expiresIn)
        return accessTokenResponse.accessToken
    }

    private fun fetchAccessToken(): AccessTokenResponse {
        val formData: Map<String, String> = mapOf(
            "grant_type" to "refresh_token",
            "client_id" to this.dropboxConnectionParams.clientId,
            "client_secret" to this.dropboxConnectionParams.secret,
            "refresh_token" to this.dropboxConnectionParams.refreshToken,
        )
        val body = toFormData(formData)
        val request = HttpRequest.newBuilder()
            .uri(URI("${dropboxConnectionParams.apiEndpoint}/oauth2/token"))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val responseText: String = response.body()
        val tokenResponse = objectMapper.readValue(responseText, AccessTokenResponse::class.java)

        return tokenResponse
    }
}
