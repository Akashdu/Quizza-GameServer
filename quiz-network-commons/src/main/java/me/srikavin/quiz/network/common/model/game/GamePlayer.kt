package me.srikavin.quiz.network.common.model.game

import me.srikavin.quiz.network.common.getString
import me.srikavin.quiz.network.common.getUUID
import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer
import java.util.*

data class GamePlayer(
    val id: UUID,
    val name: String,
    val score: Int,
    val imageUrl: String
)

fun GamePlayer.serialize(buffer: ByteBuffer) {
    buffer.put(id)
    buffer.put(name)
    buffer.putInt(score)
    buffer.put(imageUrl)
}

fun GamePlayer.countBytes(): Int {
    val array = name.toByteArray(Charsets.UTF_8).size + 4
    val imageUrlBytes = imageUrl.toByteArray(Charsets.UTF_8).size + 4
    return 16 + 4 + array + imageUrlBytes
}

fun deserializeGamePlayer(buffer: ByteBuffer): GamePlayer {
    val id = buffer.getUUID()
    val name = buffer.getString()
    val score = buffer.int
    val avatar = buffer.getString()
    return GamePlayer(id, name, score, avatar)
}

