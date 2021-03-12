package me.srikavin.quiz.network.common

import java.nio.ByteBuffer
import java.util.*

fun ByteBuffer.put(uuid: UUID) {
    this.putLong(uuid.mostSignificantBits)
    this.putLong(uuid.leastSignificantBits)
}

fun ByteBuffer.getUUID(): UUID {
    val mostSig = this.long
    val leastSig = this.long
    return UUID(mostSig, leastSig)
}

fun ByteBuffer.put(str: String) {
    val array = str.toByteArray(Charsets.UTF_8)
    this.putInt(array.size)
    this.put(array)
}

fun ByteBuffer.getString(): String {
    val length = this.int
    val array = ByteArray(length)
    this.get(array)
    return String(array, Charsets.UTF_8)
}
