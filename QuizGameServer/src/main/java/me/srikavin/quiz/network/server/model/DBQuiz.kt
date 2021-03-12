package me.srikavin.quiz.network.server.model

import com.mongodb.client.MongoCollection
import me.srikavin.quiz.network.common.model.data.QuizModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.findOneById

data class DBQuiz(
        @BsonId
        val _id: Id<QuizModel>,
        override val title: String,
        override val description: String,
        override val questions: List<DBQuizQuestion>
) : QuizModel {
    override val id: ResourceId = ResourceId(_id.toString())
}

class QuizRepository(val col: MongoCollection<DBQuiz>) {
    fun getQuiz(id: ResourceId): DBQuiz? {
        return this.getQuiz(id.idString)
    }

    fun getQuiz(id: String): DBQuiz? {
        return this.getQuiz(ObjectId(id))
    }


    fun getQuiz(id: ObjectId): DBQuiz? {
        return col.findOneById(id)
    }
}