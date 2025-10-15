package com.charlesq.greasingthegroove

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.Instant
import java.time.LocalDate

data class Exercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val exerciseMode: String = "REPS", // 'REPS' or 'DURATION'
    val isCustom: Boolean = false,
    val lastReps: Int? = null,
    val lastWeightAdded: Double? = null,
    val lastDurationSeconds: Int? = null,
    @ServerTimestamp val dateCreated: Instant? = null
)

data class ActiveGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val dailyTargetSets: Int? = null,
    val dailyTargetDurationSeconds: Int? = null,
    @ServerTimestamp val dateSet: Instant? = null
)

data class DailySetLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val date: String = "", // YYYY-MM-DD
    @ServerTimestamp val timestamp: Instant? = null,
    val weightAdded: Double? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val notes: String? = null
)
