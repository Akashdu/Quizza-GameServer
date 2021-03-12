package me.srikavin.quiz.network.common.message.matchmaker

import me.srikavin.quiz.network.common.message.MATCHMAKER_STATE_UPDATE_PACKET_ID
import me.srikavin.quiz.network.common.message.MessageBase
import me.srikavin.quiz.network.common.message.MessageSerializer
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakerStates
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakingState
import java.nio.ByteBuffer


data class MatchmakerStateUpdateMessage(val state: MatchmakingState) : MessageBase(MATCHMAKER_STATE_UPDATE_PACKET_ID)

class MatchmakerStateUpdateMessageSerializer : MessageSerializer<MatchmakerStateUpdateMessage> {
    override fun toBytes(t: MatchmakerStateUpdateMessage): ByteBuffer {
        val buffer = ByteBuffer.allocate(1 + 2 + 2)
        buffer.put(t.state.state.code)
        buffer.putShort(t.state.playersFound)
        buffer.putShort(t.state.playersMax)
        return buffer
    }

    override fun fromBytes(buffer: ByteBuffer): MatchmakerStateUpdateMessage {
        val code = buffer.get()
        val playersFound = buffer.short
        val playersMax = buffer.short
        return MatchmakerStateUpdateMessage(MatchmakingState(MatchmakerStates.fromCode(code), playersFound, playersMax))
    }

}