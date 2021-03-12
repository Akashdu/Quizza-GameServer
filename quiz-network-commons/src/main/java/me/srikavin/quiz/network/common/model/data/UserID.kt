package me.srikavin.quiz.network.common.model.data

import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer
import java.util.*

inline class UserID(val id: UUID){
    constructor(buffer: ByteBuffer) : this(UUID(buffer.long, buffer.long))
}

fun ByteBuffer.put(token: UserID) {
    this.put(token.id)
}