package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.Collections.synchronizedSet
import java.util.concurrent.atomic.AtomicInteger

class Connection (val session: DefaultWebSocketServerSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
}

fun Application.configureSockets() {

    val connections = synchronizedSet<Connection>(LinkedHashSet())

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/chat") {
            println("*new connection")// websocketSession
            val thisConnection = Connection(this)
            connections += thisConnection

            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    connections.forEach{
                        it.session.send(receivedText)
                    }
                }
            } catch (e:Exception) {
                error(e.localizedMessage)
            } finally {
                connections -= thisConnection
            }
        }
    }
}