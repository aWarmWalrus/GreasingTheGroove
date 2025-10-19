package com.charlesq.greasingthegroove

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class BodyPart {
    ARMS,
    LEGS,
    CHEST,
    BACK,
    CORE
}

enum class MovementPattern {
    PUSH,
    PULL,
    LUNGE,
    SQUAT,
    HINGE,
    CORE_AND_CARRY
}

data class Exercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val metric: MetricType = MetricType.REPS,
    val isCustom: Boolean = false,
    val primaryTarget: BodyPart? = null,
    val otherTargets: List<BodyPart> = emptyList(),
    val movementPattern: MovementPattern? = null
)

data class CompletedSet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val date: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val reps: Int? = null,
    val durationSeconds: Double? = null,
    val weightAdded: Double? = null,
    val userCompletedAt: String? = null,
    val notes: String? = null
)
