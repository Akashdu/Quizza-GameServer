package me.srikavin.quiz.network.server

import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import java.net.ServerSocket

fun main(args: Array<String>) {
    var port = 1200
    var dbName = "quizza"
    var mongoHost = "127.0.1.1"
    var mongoPort = 27017
    if (args.isNotEmpty()) {
        port = args[0].toInt()
    }
    if (args.size >= 2) {
        dbName = args[1]
    }
    if (args.size >= 4) {
        mongoHost = args[2]
        mongoPort = args[3].toInt()
    }

    val socket = ServerSocket(port)
    val server = Server(socket)

    val client = KMongo.createClient(mongoHost, mongoPort)
    val database: MongoDatabase = client.getDatabase(dbName)

    server.start(database)
}