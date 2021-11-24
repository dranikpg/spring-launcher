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
        authenticate("admin") {
            get("/status") {
                if (Launcher.BUSY.get()) call.respond("busy")
                else call.respond("ready")
            }
        }
    }
}
