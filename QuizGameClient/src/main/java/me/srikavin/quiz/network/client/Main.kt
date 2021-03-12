package me.srikavin.quiz.network.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.srikavin.quiz.network.common.MessageRouter
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.net.InetAddress
import java.util.*

fun main() {
    val messageRouter = MessageRouter()
    val client = NetworkClient(InetAddress.getByName("localhost"), messageRouter)
    client.start(CoroutineScope(Dispatchers.IO), null as UUID?)

    val gameClient = NetworkGameClient(client, messageRouter)
    gameClient.startMatchmaking(ResourceId("5c2fc72132095420d9e8f929"))

    runBlocking {
        delay(100000)
    }
}