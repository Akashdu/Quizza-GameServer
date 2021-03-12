package me.srikavin.quiz.network.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.*

internal class UtilKtTest {

    @Test
    fun getUUID() {
        val uuid: UUID = UUID.randomUUID()
        val buffer = ByteBuffer.allocate(16)
        buffer.put(uuid)

        buffer.rewind()

        assertEquals(uuid, buffer.getUUID())
        assertEquals(16, buffer.position())
    }

    @Test
    fun getString() {
        val str = "Testing string of a variable length"
        val buffer = ByteBuffer.allocate(str.toByteArray(Charsets.UTF_8).size + 4)
        buffer.put(str)

        buffer.rewind()

        assertEquals(str, buffer.getString())
    }

    @Test
    fun getStringNonAscii() {
        val str = "Testing «ταБЬℓσ»: 1<2 & 4+1>3, 20%!"
        val buffer = ByteBuffer.allocate(str.toByteArray(Charsets.UTF_8).size + 4)
        buffer.put(str)

        buffer.rewind()

        assertEquals(str, buffer.getString())
    }
}