package me.srikavin.quiz.network.server

import me.srikavin.quiz.network.common.model.network.RejoinToken
import java.nio.ByteBuffer
import java.util.*


class AuthService {
    private val tokenMap = HashMap<RejoinToken, UUID>()

    fun getRejoinToken(buffer: ByteBuffer) : RejoinToken {
        return RejoinToken(UUID(buffer.long, buffer.long))
    }

    fun getUUID(token: RejoinToken?): Pair<RejoinToken, UUID> {
        if (token != null && tokenMap.containsKey(token)) {
            return Pair(token, tokenMap[token]!!)
        }

        val genToken = RejoinToken(UUID.randomUUID())
        val genUUID = UUID.randomUUID()

        tokenMap[genToken] = genUUID

        return Pair(genToken, genUUID)
    }
}