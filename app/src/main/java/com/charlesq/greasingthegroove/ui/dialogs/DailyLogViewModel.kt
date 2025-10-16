package com.charlesq.greasingthegroove.ui.dialogs

import androidx.lifecycle.ViewModel
import com.charlesq.greasingthegroove.CompletedSet
import com.charlesq.greasingthegroove.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExerciseStats(
    val totalSets: Int,
    val totalReps: Int?,
    val totalDuration: Int?
)

data class DailyLogUiState(
    val setsByExercise: Map<Exercise?, List<CompletedSet>> = emptyMap(),
    val expandedExerciseId: String? = null,
    val exerciseStats: Map<String, ExerciseStats> = emptyMap()
)

class DailyLogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState = _uiState.asStateFlow()

    fun processSets(
        setsForDate: List<CompletedSet>,
        predefinedExercises: Map<String, Exercise>
    ) {
        val setsByExercise = setsForDate.groupBy { set ->
            predefinedExercises[set.exerciseId]
        }

        val exerciseStats = setsByExercise.mapNotNull { (exercise, sets) ->
            exercise?.let {
                val totalSets = sets.size
                val totalReps = if (sets.any { it.reps != null }) sets.sumOf { it.reps ?: 0 } else null
                val totalDuration = if (sets.any { it.durationSeconds != null }) sets.sumOf { it.durationSeconds ?: 0 } else null
                it.id to ExerciseStats(totalSets, totalReps, totalDuration)
            }
        }.toMap()

        _uiState.update { it.copy(setsByExercise = setsByExercise, exerciseStats = exerciseStats) }
    }

    fun onExerciseClicked(exerciseId: String) {
        _uiState.update {
            if (it.expandedExerciseId == exerciseId) {
                it.copy(expandedExerciseId = null) // Collapse if already expanded
            } else {
                it.copy(expandedExerciseId = exerciseId) // Expand new one
            }
        }
    }
}
