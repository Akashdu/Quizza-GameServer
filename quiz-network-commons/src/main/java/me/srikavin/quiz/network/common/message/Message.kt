package me.srikavin.quiz.network.common.message

import java.nio.ByteBuffer

inline class MessageIdentifier(val value: Byte)

interface MessageSerializer<T : MessageBase> {
    fun toBytes(t: T): ByteBuffer
    fun fromBytes(buffer: ByteBuffer): T
}

abstract class MessageBase(val identifier: MessageIdentifier)
