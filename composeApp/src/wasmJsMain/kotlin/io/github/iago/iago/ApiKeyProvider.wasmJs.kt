package io.github.iago.iago

import kotlin.js.JsName

external interface IagoGlobalThis {
	val IAGO_BACKEND_URL: String?
}

@JsName("globalThis")
external val globalThis: IagoGlobalThis

actual fun backendBaseUrl(): String = globalThis.IAGO_BACKEND_URL ?: ""






