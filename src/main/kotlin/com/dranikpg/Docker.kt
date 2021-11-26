package com.dranikpg

// Generic docker utils
private fun runCommand(vararg cmd: String) {
    ProcessBuilder("docker", *cmd)
        .inheritIO()
        .start()
        .exitValue()
}

class DockerLaunchBuilder {
    private val commands = mutableListOf<String>("docker", "run")
    var redirectOutput = false
    fun env(key: String, value: String) {
        commands += "--env"
        commands +="${key}=${value}"
    }
    fun network(net: String) {
        commands += "--net"
        commands += net
    }
    fun expose(host: Int, container: Int) {
        commands += "-p"
        commands += "${host}:${container}"
    }
    fun build(container: String) : List<String> {
        commands += container
        return commands
    }
}

object Docker {
    fun kill(vararg names: String) = runCommand("kill", *names)
    fun rm(vararg names: String) = runCommand("rm", *names)
    fun launch(container: String, f: DockerLaunchBuilder.() -> Unit) : Process {
        val launchBuilder = DockerLaunchBuilder()
        launchBuilder.f()
        val processBuilder = ProcessBuilder(launchBuilder.build(container))
        if (launchBuilder.redirectOutput) {
            processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
        }
        return processBuilder.start()
    }
}

