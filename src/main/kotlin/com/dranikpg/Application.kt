package com.dranikpg

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

fun readLaunchConfig() : LaunchConfig {
    val f = File("launch.json")
    return Json.decodeFromStream(f.inputStream())
}

fun main() {
    val launchCfg = readLaunchConfig()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        configureSecurity()
        configureRouting()
        configureSockets(launchCfg)
        supportProtocolSwitching()
    }.start(wait = true)
}
