package com.dranikpg

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception

const val LOG_NEEDLE = "database system is ready to accept connections"

@Serializable
data class RepoData(val repo: String, val folder: String, val branch: String)

@Serializable
data class LaunchConfig(val image: String, val network: String,
                        val channelBuf: Int, val readerBuf: Int,
                        val postgresDB: String, val postgresLogin: String)


fun parseRepoUrl(url: String) : RepoData? {
    val regex = ".*?github.com\\/(.*?)\\/tree\\/(.*?)\\/(.*)".toRegex()
    val result = regex.matchEntire(url) ?: return null
    val (repo, branch, folder) = result.destructured
    return RepoData(repo, folder, branch)
}

// Launcher instance
class Launcher (val cfg: LaunchConfig) {
    // channel for sending log lines
    val channel = Channel<String>(cfg.channelBuf)

    companion object {
        val LOCK = Mutex()
    }

    // Start Postgres database in container
    private suspend fun startBD() {
        val process = Docker.launch("library/postgres") {
            env("POSTGRES_USER", cfg.postgresLogin)
            env("POSTGRES_PASSWORD", cfg.postgresLogin)
            env("POSTGRES_DB", cfg.postgresDB)
            network(cfg.network)
            redirectOutput = true
        }

        val reader = BufferedReader(InputStreamReader(process.errorStream), 40)
        for (line in reader.lines()) {
            channel.send(line)
            if (line.contains(LOG_NEEDLE)) return
        }
        reader.close()
        throw Exception("Failed to start postgres")
    }

    // Echo stream to channel
    private suspend fun echoStream(stream: InputStream) {
        val reader = BufferedReader(InputStreamReader(stream))
        for (line in reader.lines()) {
            channel.send(line)
        }
        reader.close()
    }

    private suspend fun start(url: String) {
        val repo = parseRepoUrl(url) ?: run {
            channel.send("Wrong url format")
            return
        }

        channel.send("Parsed: ${Json.encodeToString(repo)}")
        channel.send("Starting postgres with timeout...")

        withTimeout(20000) {
            startBD()
        }

        channel.send("Started postgres. Running docker")

        val process = Docker.launch(cfg.image) {
            env("REPO_URL", repo.repo)
            env("FOLDER", repo.folder)
            env("BRANCH", repo.branch)
            network(cfg.network)
            expose(80, 80)
            redirectOutput = true
        }

        coroutineScope {
            val h1 = async(Dispatchers.IO) { echoStream(process.inputStream) }
            val h2 = async(Dispatchers.IO) { echoStream(process.errorStream) }
            h1.await()
            h2.await()
        }
    }

    suspend fun message(msg: String) {
        while (true) {
            if (LOCK.tryLock()) {
                break;
            }
            channel.send("Launcher busy... waiting")
        }
        GlobalScope.launch(Dispatchers.IO) { start(msg) }
    }

    fun stop() {
        GlobalScope.launch (Dispatchers.IO) {
            Docker.kill("app")
            Docker.kill("db")
            Docker.rm("app")
            Docker.rm("db")
            LOCK.unlock()
        }
    }
}
