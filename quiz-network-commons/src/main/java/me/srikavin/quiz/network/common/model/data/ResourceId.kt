package me.srikavin.quiz.network.common.model.data

import me.srikavin.quiz.network.common.getString
import me.srikavin.quiz.network.common.put
import java.nio.ByteBuffer

inline class ResourceId(val idString: String)

fun ResourceId.countBytes(): Int {
    return this.idString.toByteArray(Charsets.UTF_8).size + 4
}

fun ByteBuffer.put(id: ResourceId) {
    this.put(id.idString)
}

fun ByteBuffer.getResourceId(): ResourceId {
    return ResourceId(this.getString())
}