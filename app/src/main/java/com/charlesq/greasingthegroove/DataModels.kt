package com.charlesq.greasingthegroove

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Exercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val metric: MetricType = MetricType.REPS,
    val isCustom: Boolean = false
)

data class CompletedSet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val date: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val weightAdded: Double? = null,
    val userCompletedAt: String? = null,
    val notes: String? = null
)
