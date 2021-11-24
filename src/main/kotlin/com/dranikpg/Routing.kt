package com.dranikpg

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.request.*

fun Application.configureRouting() {
    routing {
        static {
            files("client/build/")
            default("client/build/index.html")
        }
        authenticate("admin") {
            get("/api/status") {
                if (Launcher.BUSY.get()) call.respond("busy")
                else call.respond("ready")
            }
        }
    }
}
