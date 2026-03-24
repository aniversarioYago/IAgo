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
import io.ktor.server.application.call
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
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("IAgoBackend")

private const val DEFAULT_MODEL = "gemini-3.0-flash"
private const val FALLBACK_MODEL = "gemini-2.5-flash"
private const val API_VERSION = "v1"
private val DEFAULT_ALLOWED_ORIGINS = setOf("https://aniversarioyago.github.io")
private const val SYSTEM_INSTRUCTION = "Você é um assistente amigável e útil chamado IAgo. " +
    "Sempre responda em português (Brasil), a menos que o usuário peça explicitamente uma resposta em outro idioma. " +
    "Seja conciso, claro e prestativo em suas respostas."

private val json = Json {
    ignoreUnknownKeys = true
}

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
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
        ?: DEFAULT_ALLOWED_ORIGINS

    val allowLocalhostCors = System.getenv("IAGO_ALLOW_LOCALHOST_CORS")
        ?.equals("true", ignoreCase = true)
        ?: false

    routing {
        get("/health") {
            call.respondText("ok")
        }

        options("/api/chat") {
            call.appendCorsHeaders(allowedOrigins, allowLocalhostCors)
            call.respondText("ok")
        }

        post("/api/chat") {
            call.appendCorsHeaders(allowedOrigins, allowLocalhostCors)
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

            val model = System.getenv("GEMINI_MODEL")?.trim().orEmpty().ifBlank { DEFAULT_MODEL }

            try {
                logger.info("Enviando request ao Gemini: $message")
                
                val contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = SYSTEM_INSTRUCTION)),
                    ),
                    GeminiContent(
                        role = "model",
                        parts = listOf(GeminiPart(text = "Entendi. Vou sempre responder em português Brasil.")),
                    ),
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = message)),
                    ),
                )
                
                val reply = generateReplyWithFallback(
                    apiKey = apiKey,
                    preferredModel = model,
                    contents = contents,
                )
                
                logger.info("Sucesso: $reply")
                call.respond(ChatResponse(reply = reply, error = null))
            } catch (error: Exception) {
                logger.error("Erro: ${error.message}")
                call.respond(
                    HttpStatusCode.BadGateway,
                    ChatResponse(reply = "", error = mapUnhandledGeminiExceptionMessage(error)),
                )
            }
        }
    }
}

private suspend fun generateReplyWithFallback(
    apiKey: String,
    preferredModel: String,
    contents: List<GeminiContent>,
): String {
    val candidates = listOf(preferredModel, FALLBACK_MODEL).distinct()
    var lastError: Throwable? = null

    for ((index, modelCandidate) in candidates.withIndex()) {
        try {
            val response = httpClient.post(
                "https://generativelanguage.googleapis.com/$API_VERSION/models/$modelCandidate:generateContent?key=$apiKey",
            ) {
                contentType(ContentType.Application.Json)
                setBody(GeminiGenerateContentRequest(contents = contents))
            }.body<GeminiGenerateContentResponse>()

            logger.info("Resposta Gemini recebida (modelo=$modelCandidate): $response")
            return extractModelReply(response) ?: error("Resposta vazia do Gemini.")
        } catch (error: Exception) {
            lastError = error
            val message = error.message.orEmpty()
            val canFallback =
                message.contains("not found", ignoreCase = true) ||
                message.contains("not supported", ignoreCase = true)

            if (canFallback && index < candidates.lastIndex) {
                logger.warn("Modelo indisponível ($modelCandidate). Tentando fallback: ${candidates[index + 1]}")
                continue
            }
            throw error
        }
    }

    throw lastError ?: IllegalStateException("Falha ao gerar resposta do Gemini.")
}

private fun io.ktor.server.application.ApplicationCall.appendCorsHeaders(
    allowedOrigins: Set<String>,
    allowLocalhostCors: Boolean,
) {
    val requestOrigin = request.headers["Origin"]
    if (requestOrigin != null && isOriginAllowed(requestOrigin, allowedOrigins, allowLocalhostCors)) {
        response.headers.append("Access-Control-Allow-Origin", requestOrigin)
        response.headers.append("Vary", "Origin")
    }
    response.headers.append("Access-Control-Allow-Methods", "POST, OPTIONS")
    response.headers.append("Access-Control-Allow-Headers", "Content-Type")
}

private fun isOriginAllowed(origin: String, allowedOrigins: Set<String>, allowLocalhostCors: Boolean): Boolean {
    if ("*" in allowedOrigins) return true
    if (origin in allowedOrigins) return true
    if (!allowLocalhostCors) return false

    return origin.startsWith("http://localhost:") ||
        origin.startsWith("http://127.0.0.1:") ||
        origin.startsWith("http://0.0.0.0:")
}

private fun extractModelReply(response: GeminiGenerateContentResponse): String? {
    if (response.error != null) {
        error(mapGeminiErrorMessage(response.error))
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

private fun mapGeminiErrorMessage(error: GeminiError): String {
    val rawMessage = error.message.orEmpty()
    val exceededQuota = isQuotaExceededMessage(rawMessage)

    if (exceededQuota) {
        return "Voce ultrapassou o limite diario de 20 requisicoes."
    }

    return "Erro da API: ${error.message} (${error.status})"
}

private fun mapUnhandledGeminiExceptionMessage(error: Throwable): String {
    val rawMessage = error.message.orEmpty()
    if (isQuotaExceededMessage(rawMessage)) {
        return "Voce ultrapassou o limite diario de 20 requisicoes."
    }
    return rawMessage.ifBlank { "Falha no backend." }
}

private fun isQuotaExceededMessage(rawMessage: String): Boolean {
    return rawMessage.contains("exceeded your current quota", ignoreCase = true) ||
        rawMessage.contains("rate-limits", ignoreCase = true)
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

