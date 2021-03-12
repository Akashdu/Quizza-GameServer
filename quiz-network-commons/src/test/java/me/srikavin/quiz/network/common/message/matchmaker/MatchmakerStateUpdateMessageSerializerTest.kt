package me.srikavin.quiz.network.common.message.matchmaker

import me.srikavin.quiz.network.common.model.matchmaker.MatchmakerStates
import me.srikavin.quiz.network.common.model.matchmaker.MatchmakingState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MatchmakerStateUpdateMessageSerializerTest {
    @Test
    fun testSerialization(){
        val original = MatchmakerStateUpdateMessage(MatchmakingState(MatchmakerStates.MATCH_FOUND, 2, 2))

        val serializer = MatchmakerStateUpdateMessageSerializer()

        val serializedBytes = serializer.toBytes(original)
        serializedBytes.flip()
        val serialized = serializer.fromBytes(serializedBytes)

        assertEquals(original, serialized)
    }
}
