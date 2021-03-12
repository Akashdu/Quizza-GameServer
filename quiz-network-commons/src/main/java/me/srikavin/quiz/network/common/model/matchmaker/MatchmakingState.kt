package me.srikavin.quiz.network.common.model.matchmaker

enum class MatchmakerStates(val code: Byte) {
    SEARCHING(1),
    STOPPED(2),
    MATCH_FOUND(3);

    companion object {
        fun fromCode(code: Byte): MatchmakerStates {
            for (e in values()) {
                if (e.code == code) {
                    return e
                }
            }
            throw RuntimeException("Unknown code received from client: $code")
        }
    }
}

data class MatchmakingState(val state: MatchmakerStates, val playersFound: Short, val playersMax: Short)