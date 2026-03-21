# IAgo Chatbot - "Fail to Fetch" Issue - FIXED ✅

## Problem Summary
The IAgo chatbot was responding with "Fail to fetch" errors whenever users tried to send messages to the Gemini API through the backend.

## Root Causes Identified

### 1. **Deprecated Gemini Model** ❌
- **Issue**: The backend was using `gemini-1.5-flash` model with the `v1beta` API
- **Problem**: This model is no longer available in the Gemini API
- **Symptom**: Received error: "models/gemini-1.5-flash is not found for API version v1beta"

### 2. **Missing Error Handling** ❌
- **Issue**: The backend wasn't checking for error responses from the Gemini API
- **Problem**: When the API returned an error, the response had `candidates=null`, causing the backend to fail silently with "Resposta vazia do Gemini"
- **Symptom**: No useful error messages to debug the issue

### 3. **Wrong API Endpoint Version** ❌
- **Issue**: Using deprecated `v1beta` instead of stable `v1` API
- **Problem**: Incompatibility with newer Gemini models

## Solutions Applied

### File Modified: `/home/kayque/Repos/IAgo/backend/src/main/kotlin/io/github/iago/backend/Main.kt`

#### Change 1: Updated Model and API Version
```kotlin
// Before
private const val DEFAULT_MODEL = "gemini-1.5-flash"

// After
private const val DEFAULT_MODEL = "gemini-2.5-flash"
private const val API_VERSION = "v1"
```

#### Change 2: Updated API Endpoint URL
```kotlin
// Before
val response = httpClient.post(
    "https://generativelanguage.googleapis.com/v1beta/models/$DEFAULT_MODEL:generateContent?key=$apiKey",
)

// After
val response = httpClient.post(
    "https://generativelanguage.googleapis.com/$API_VERSION/models/$DEFAULT_MODEL:generateContent?key=$apiKey",
)
```

#### Change 3: Added Error Response Handling
```kotlin
// Before
@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
)

// After
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
```

#### Change 4: Updated extractModelReply Function
```kotlin
// Before
private fun extractModelReply(response: GeminiGenerateContentResponse): String? {
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

// After
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
```

## How to Run the Backend

```bash
cd /home/kayque/Repos/IAgo
PORT=8081 GEMINI_API_KEY="AIzaSyBdCccCTRoAJW7ss58Eclc7IwypNTphS0o" ./gradlew backend:run
```

Or with nohup for persistent background execution:
```bash
nohup bash -c 'PORT=8081 GEMINI_API_KEY="AIzaSyBdCccCTRoAJW7ss58Eclc7IwypNTphS0o" ./gradlew backend:run' > /tmp/backend.log 2>&1 &
```

## Frontend Configuration
The frontend is already correctly configured in `/home/kayque/Repos/IAgo/composeApp/src/webMain/resources/index.html`:
```javascript
window.IAGO_BACKEND_URL = "http://localhost:8081";
```

## Verification Tests ✅

### Test 1: Health Check
```bash
$ curl http://localhost:8081/health
ok
```
✅ **PASSED**

### Test 2: Chat API with Question 1
```bash
$ curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Ola, quem eh voce?"}'
  
Response: {"reply":"Olá! Eu sou um modelo de linguagem grande, treinado pelo Google..."}
```
✅ **PASSED**

### Test 3: Chat API with Question 2
```bash
$ curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Qual eh a capital do Brasil?"}'
  
Response: {"reply":"A capital do Brasil é **Brasília**."}
```
✅ **PASSED**

## Backend Logs Confirmation
The backend logs show successful responses from the Gemini API:
```
12:55:37.707 [eventLoopGroupProxy-4-3] INFO IAgoBackend -- Enviando request ao Gemini: Ola, quem eh voce?
12:55:40.889 [eventLoopGroupProxy-4-3] INFO IAgoBackend -- Resposta Gemini recebida: GeminiGenerateContentResponse(candidates=[GeminiCandidate(...)], error=null)
12:55:40.889 [eventLoopGroupProxy-4-3] INFO IAgoBackend -- Sucesso: Olá! Eu sou um modelo de linguagem grande...
```

## Architecture
- **Backend**: Kotlin Ktor server on port 8081
- **Frontend**: Kotlin Compose Multiplatform (Web & Android)
- **External API**: Google Gemini 2.5 Flash API (v1)
- **CORS**: Configured to allow requests from localhost

## Status
✅ **ISSUE RESOLVED** - The chatbot is now fully functional and responding correctly to user messages.

