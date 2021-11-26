package com.dranikpg

import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlin.random.Random

data class UserSession(val admin: Boolean = false) : Principal

object LaunchTokens {
    private val tokens = mutableSetOf<String>()
    private val LOCK = Mutex()
    private fun randomToken() : String {
        // 40 122
        return (1..32).map { Char(40 + Random.nextInt(82)) }
            .joinToString("")
    }
    suspend fun generate(): String = LOCK.withLock {
        var token = randomToken()
        while (!tokens.add(token)) {
            token = randomToken()
        }
        return token
    }

    suspend fun verify(token: String) : Boolean = LOCK.withLock { tokens.remove(token) }
}

fun Application.configureSecurity() {
    val adminPassword = "admin"
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
        post("/api/login") {
            @Serializable
            data class Credentials(val password: String)
            val credentials = call.receive<Credentials>()
            if (credentials.password == adminPassword) {
                call.sessions.set(UserSession(admin = true))
                call.respond(Unit)
            } else {
                call.respond(HttpStatusCode.BadRequest, Unit)
            }
        }
        authenticate ("admin") {
            get("/api/user") { call.respond(Unit) }
        }
    }
}


fun Application.supportProtocolSwitching() {
    routing {
        authenticate ("admin") {
            get("launch") {
                call.respond(LaunchTokens.generate())
            }
        }
    }
}