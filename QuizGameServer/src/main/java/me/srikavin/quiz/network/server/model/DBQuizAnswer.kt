package me.srikavin.quiz.network.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.litote.kmongo.Id

data class DBQuizAnswer(
        @BsonId
        val _id: Id<QuizModel>,
        @BsonProperty(value = "text")
        @JsonProperty(value = "text")
        override val contents: String,
        @BsonProperty(value = "correct")
        @JsonProperty(value = "correct")
        override val isCorrect: Boolean
) : QuizAnswerModel {
    override val id: ResourceId = ResourceId(_id.toString())
}