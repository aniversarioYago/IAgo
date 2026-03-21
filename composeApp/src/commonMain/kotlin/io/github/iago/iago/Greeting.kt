package io.github.iago.iago

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val jsonConfig = Json {
    ignoreUnknownKeys = true
}

private val geminiHttpClient = HttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
    }
    install(ContentNegotiation) {
        json(jsonConfig)
    }
}

class GeminiRepository(
    private val client: HttpClient = geminiHttpClient,
) {
    suspend fun sendMessage(prompt: String): Result<String> {
        val baseUrl = backendBaseUrl().trim().trimEnd('/')
        val endpoint = if (baseUrl.isBlank()) "/api/chat" else "$baseUrl/api/chat"
        if (prompt.isBlank()) {
            return Result.failure(IllegalStateException("Mensagem vazia."))
        }

        return runCatching {
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(message = prompt))
            }.body<ChatResponse>()

            if (!response.error.isNullOrBlank()) {
                error(response.error)
            }
            response.reply.takeIf { it.isNotBlank() }
                ?: error("O backend nao retornou conteúdo.")
        }.recoverCatching { throwable ->
            throw mapNetworkError(throwable, endpoint)
        }
    }
}

private fun mapNetworkError(throwable: Throwable, endpoint: String): Throwable {
    val originalMessage = throwable.message.orEmpty()
    val failedToFetch = originalMessage.contains("Failed to fetch", ignoreCase = true)

    if (failedToFetch || throwable is HttpRequestTimeoutException || throwable is TimeoutCancellationException) {
        return IllegalStateException(
            "Falha de conexao com o backend em $endpoint. Confirme se ele esta rodando e acessivel.",
            throwable,
        )
    }

    return throwable
}

@Serializable
data class ChatRequest(
    val message: String,
)

@Serializable
data class ChatResponse(
    val reply: String,
    val error: String? = null,
)

