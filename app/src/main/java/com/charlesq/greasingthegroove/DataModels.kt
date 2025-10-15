package com.charlesq.greasingthegroove

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Exercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val exerciseMode: String = "REPS", // 'REPS' or 'ISOMETRICS'
    val isCustom: Boolean = false
)

data class ActiveGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val goalFrequency: String = "DAILY", // "DAILY" or "WEEKLY"
    val targetType: String = "SETS", // "SETS", "REPS", "SECONDS", "MINUTES"
    val targetValue: Int = 0,
    @ServerTimestamp val dateSet: Date? = null
)

data class DailySetLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val date: String = "", // YYYY-MM-DD
    @ServerTimestamp val timestamp: Date? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val weightAdded: Double? = null,
    val userCompletedAt: String? = null, // For user-provided time
    val notes: String? = null
)
