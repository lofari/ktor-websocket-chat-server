package com.example.plugins

import com.example.Connection
import io.ktor.network.sockets.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import java.lang.Exception
import java.util.*

fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding User...")
            val thisConnection = com.example.Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are  ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUserName = "[${thisConnection.name}]: $receivedText "
                    connections.forEach {
                        it.session.send(textWithUserName)
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection")
                connections -= thisConnection
            }
        }
    }

}
