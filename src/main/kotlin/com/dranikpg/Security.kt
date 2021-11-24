package com.dranikpg

import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

fun Application.configureSecurity() {
    val adminPassword = "admin"
    data class UserSession(val admin: Boolean = false) : Principal
    install(Sessions) {
        cookie<UserSession>("user", storage = SessionStorageMemory()) {}
    }
    install (Authentication) {
        session<UserSession>("admin") {
            validate { session -> if (session.admin) session else null }
            challenge { call.respond(HttpStatusCode.Unauthorized, Unit)}
        }
    }
    routing {
        post("/login") {
            @Serializable
            data class Credentials(val password: String)
            val credentials = call.receive<Credentials>()
            if (credentials.password == adminPassword) {
                call.sessions.set(UserSession(admin = true))
            }
			call.respond(Unit)
        }
        authenticate ("admin") {
            get("/user") { call.respond(Unit) }
        }
    }
}
