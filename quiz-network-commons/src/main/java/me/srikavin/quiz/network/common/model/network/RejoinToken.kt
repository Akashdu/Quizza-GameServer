package me.srikavin.quiz.network.common.model.network

import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer
import java.util.*

inline class RejoinToken(val token: UUID) {
    constructor(buffer: ByteBuffer) : this(UUID(buffer.long, buffer.long))
}

fun ByteBuffer.put(token: RejoinToken) {
    this.put(token.token)
}

