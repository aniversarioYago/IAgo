package io.github.iago.iago

actual fun backendBaseUrl(): String {
    val url = js("window.IAGO_BACKEND_URL") as? String
    return url ?: ""
}


