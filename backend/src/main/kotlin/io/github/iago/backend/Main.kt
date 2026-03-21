package io.github.iago.backend

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.options
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.util.logging.error
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("IAgoBackend")

private const val DEFAULT_MODEL = "gemini-2.5-flash"
private const val API_VERSION = "v1"

private val json = Json {
    ignoreUnknownKeys = true
}

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

@Suppress("unused")
fun Application.module() {
    install(ServerContentNegotiation) {
        json(json)
    }
    val allowedOrigins = System.getenv("IAGO_ALLOWED_ORIGINS")
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.toSet()
        ?.takeIf { it.isNotEmpty() }
        ?: setOf("*")

    routing {
        get("/health") {
            call.respondText("ok")
        }

        options("/api/chat") {
            call.appendCorsHeaders(allowedOrigins)
            call.respondText("ok")
        }

        post("/api/chat") {
            call.appendCorsHeaders(allowedOrigins)
            val request = call.receive<ChatRequest>()
            val message = request.message.trim()
            if (message.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ChatResponse(reply = "", error = "Mensagem vazia."))
                return@post
            }

            val apiKey = System.getenv("GEMINI_API_KEY")?.trim().orEmpty()
            if (apiKey.isBlank()) {
                call.respond(HttpStatusCode.InternalServerError, ChatResponse(reply = "", error = "GEMINI_API_KEY nao configurada no backend."))
                return@post
            }

            val result = runCatching {
                logger.info("Enviando request ao Gemini: $message")
                val response = httpClient.post(
                    "https://generativelanguage.googleapis.com/$API_VERSION/models/$DEFAULT_MODEL:generateContent?key=$apiKey",
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        GeminiGenerateContentRequest(
                            contents = listOf(
                                GeminiContent(
                                    role = "user",
                                    parts = listOf(GeminiPart(text = message)),
                                ),
                            ),
                        ),
                    )
                }.body<GeminiGenerateContentResponse>()

                logger.info("Resposta Gemini recebida: $response")
                extractModelReply(response) ?: error("Resposta vazia do Gemini.")
            }

            result.onSuccess { reply ->
                logger.info("Sucesso: $reply")
                call.respond(ChatResponse(reply = reply, error = null))
            }.onFailure { error ->
                logger.error("Erro: ${error.message}")
                call.respond(HttpStatusCode.BadGateway, ChatResponse(reply = "", error = error.message ?: "Falha no backend."))
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.appendCorsHeaders(allowedOrigins: Set<String>) {
    val requestOrigin = request.headers["Origin"]
    if (requestOrigin != null && isOriginAllowed(requestOrigin, allowedOrigins)) {
        response.headers.append("Access-Control-Allow-Origin", requestOrigin)
        response.headers.append("Vary", "Origin")
    }
    response.headers.append("Access-Control-Allow-Methods", "POST, OPTIONS")
    response.headers.append("Access-Control-Allow-Headers", "Content-Type")
}

private fun isOriginAllowed(origin: String, allowedOrigins: Set<String>): Boolean {
    if ("*" in allowedOrigins) return true
    if (origin in allowedOrigins) return true
    return origin.startsWith("http://localhost:") ||
        origin.startsWith("http://127.0.0.1:") ||
        origin.startsWith("http://0.0.0.0:")
}

private fun extractModelReply(response: GeminiGenerateContentResponse): String? {
    // Check if there's an error from the Gemini API
    if (response.error != null) {
        error("Erro da API Gemini: ${response.error.message} (${response.error.status})")
    }
    
    return response.candidates
        .orEmpty()
        .firstOrNull()
        ?.content
        ?.parts
        .orEmpty()
        .firstOrNull()
        ?.text
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
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

@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String,
)

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null,
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null,
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
)













