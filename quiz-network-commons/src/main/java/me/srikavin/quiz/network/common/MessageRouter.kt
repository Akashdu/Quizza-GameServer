package me.srikavin.quiz.network.common

import me.srikavin.quiz.network.common.message.*
import me.srikavin.quiz.network.common.message.game.AnswerQuestionSerializer
import me.srikavin.quiz.network.common.message.game.AnswerResponseSerializer
import me.srikavin.quiz.network.common.message.game.GameEndMessageSerializer
import me.srikavin.quiz.network.common.message.game.StateUpdateMessageSerializer
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakerStateUpdateMessageSerializer
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakingStartMessageSerializer
import me.srikavin.quiz.network.common.message.matchmaker.MatchmakingStopMessageSerializer
import me.srikavin.quiz.network.common.model.game.GameClient
import java.nio.ByteBuffer

interface MessageHandler<in T : MessageBase> {
    fun handle(client: GameClient, message: T) {

    }
}

class MessageRouter(initDefaults: Boolean = true) {
    private val packetMap: MutableMap<MessageIdentifier, MessageSerializer<out MessageBase>> = HashMap()
    private val handlerMap: MutableMap<MessageIdentifier, MutableSet<MessageHandler<MessageBase>>> =
        mutableMapOf<MessageIdentifier, MutableSet<MessageHandler<MessageBase>>>().withDefault { HashSet() }

    init {
        if (initDefaults) {
            registerPacket(MATCHMAKER_START_PACKET_ID, MatchmakingStartMessageSerializer())
            registerPacket(MATCHMAKER_STOP_PACKET_ID, MatchmakingStopMessageSerializer())
            registerPacket(MATCHMAKER_STATE_UPDATE_PACKET_ID, MatchmakerStateUpdateMessageSerializer())
            registerPacket(STATE_UPDATE_PACKET_ID, StateUpdateMessageSerializer())
            registerPacket(ANSWER_QUESTION_PACKET_ID, AnswerQuestionSerializer())
            registerPacket(GAME_END_PACKET_ID, GameEndMessageSerializer())
            registerPacket(ANSWER_RESPONSE_PACKET_ID, AnswerResponseSerializer())
        }
    }

    fun handlePacket(client: GameClient, message: ByteBuffer) {
        val id = MessageIdentifier(message.get())
        val serializer = packetMap[id] ?: throw RuntimeException("Unknown packet id: $id")
        val packet = serializer.fromBytes(message)
        handlePacket(client, packet)
    }

    fun handlePacket(client: GameClient, message: MessageBase) {
        handlerMap[message.identifier].orEmpty().forEach {
            it.handle(client, message)
        }
    }

    fun serializeMessage(message: MessageBase): ByteBuffer {
        val serializer = packetMap[message.identifier]
            ?: throw RuntimeException("Unrecognized packet: ${message.identifier}; $message, has it been registered?)")


        @Suppress("UNCHECKED_CAST")
        return (serializer as MessageSerializer<MessageBase>).toBytes(message)
    }

    fun registerPacket(type: MessageIdentifier, serializer: MessageSerializer<out MessageBase>) {
        packetMap[type] = serializer
    }

    fun <T : MessageBase> registerHandler(type: MessageIdentifier, handler: MessageHandler<T>) {
        if (!handlerMap.containsKey(type)) {
            handlerMap[type] = HashSet()
        }
        @Suppress("UNCHECKED_CAST")
        handlerMap[type]?.add(handler as MessageHandler<MessageBase>)
    }
}