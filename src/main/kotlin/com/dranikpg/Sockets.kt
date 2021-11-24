package com.dranikpg

import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select

suspend inline fun <R> listen(crossinline builder: SelectBuilder<R>.() -> Unit) {
    while (true) select(builder)
}

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/launch") {
            val launcher = Launcher.createwCAS() ?: return@webSocket
            try {
                if (!isActive) return@webSocket
                listen<Unit> {
                    launcher.channel.onReceive { msg -> send(msg) }
                    incoming.onReceive { frame ->
                        if (frame is Frame.Text) launcher.message(frame.readText())
                    }
                }
            } finally {
                launcher.stop()
            }
        }
    }
}
