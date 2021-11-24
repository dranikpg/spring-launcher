package com.dranikpg

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

const val LOG_NEEDLE = "database system is ready to accept connections"

@Serializable
data class RepoData(val repo: String, val folder: String, val branch: String)

@Serializable
data class LaunchConfig(val image: String, val network: String, val channelBuf: Int, val readerBuf: Int)

suspend inline fun echoStream(stream: InputStream, channel: Channel<String>, bufSize: Int) {
    val reader = BufferedReader(InputStreamReader(stream), bufSize)
    for (line in reader.lines()) {
        channel.send(line)
    }
    reader.close()
}

fun runCommand(vararg cmd: String) {
    ProcessBuilder(*cmd)
        .inheritIO()
        .start()
        .waitFor()
}

fun parseRepoUrl(url: String) : RepoData? {
    val regex = ".*?github.com\\/(.*?)\\/tree\\/(.*?)\\/(.*)".toRegex()
    val result = regex.matchEntire(url) ?: return null
    val (repo, branch, folder) = result.destructured
    return RepoData(repo, folder, branch)
}

// Launcher instance
class Launcher private constructor () {
    companion object {
        var CONFIG = LaunchConfig("spring-launcher", "lesson", 10, 50)
        val BUSY = AtomicBoolean(false)
        fun createwCAS() : Launcher? {
            if (BUSY.compareAndSet(false, true)) {
                return Launcher()
            }
            return null
        }
        fun readConfig(f: File) {
            CONFIG = Json.decodeFromStream(f.inputStream())
        }
    }
    // channel for sending log lines
    val channel = Channel<String>(CONFIG.channelBuf)

    suspend fun startBD() {
        val process = ProcessBuilder("docker", "run",
                "--env", "POSTGRES_USER=lesson",
                "--env", "POSTGRES_PASSWORD=lesson",
                "--env", "POSTGRES_DB=lesson",
                "--name", "db",
                "--net", CONFIG.network,
                "library/postgres")
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val reader = BufferedReader(InputStreamReader(process.errorStream), 40)
        for (line in reader.lines()) {
            channel.send(line)
            if (line.contains(LOG_NEEDLE)) return
        }
        reader.close()
        throw Exception("Failed to start postgres")
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

        channel.send("Started postgres")
        channel.send("Running docker image")

        val process = ProcessBuilder("docker", "run",
                "--env", "REPO_URL=${repo.repo}",
                "--env", "FOLDER=${repo.folder}",
                "--env", "BRANCH=${repo.branch}",
				"--net", CONFIG.network,
                "--name", "app",
				"-p", "80:80",
            	CONFIG.image)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        coroutineScope {
            val h1 = async(Dispatchers.IO) { echoStream(process.inputStream, channel, CONFIG.readerBuf) }
            val h2 = async(Dispatchers.IO) { echoStream(process.errorStream, channel, CONFIG.readerBuf) }
            h1.await()
            h2.await()
        }
    }

    suspend fun message(msg: String) {
        GlobalScope.launch(Dispatchers.IO) { start(msg) }
    }

    fun stop() {
        GlobalScope.launch (Dispatchers.IO) {
            runCommand("docker", "kill", "app")
            runCommand("docker", "kill", "db")
            runCommand("docker", "rm", "app")
            runCommand("docker", "rm", "db")
            BUSY.set(false)
        }
    }
}
