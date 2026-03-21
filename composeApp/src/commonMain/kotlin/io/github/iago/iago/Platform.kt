package io.github.iago.iago

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
