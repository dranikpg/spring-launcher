package com.dranikpg

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import java.io.File

fun checkConfig() {
    val f = File("launch.json")
    if (f.exists()) {
        Launcher.readConfig(f)
    }
}

fun main() {
    checkConfig()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        configureSecurity()
        configureRouting()
        configureSockets()
    }.start(wait = true)
}
