package com.charlesq.greasingthegroove

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExercisePickerUiState(
    val exercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val selectedMovementPattern: MovementPattern? = null,
    val selectedBodyPart: BodyPart? = null
)

class ExercisePickerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExercisePickerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        val exercises = getPredefinedExercises()
        _uiState.update {
            it.copy(exercises = filterAndSortExercises(exercises, it.searchQuery, it.selectedMovementPattern, it.selectedBodyPart))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                exercises = filterAndSortExercises(getPredefinedExercises(), query, it.selectedMovementPattern, it.selectedBodyPart)
            )
        }
    }

    fun onMovementPatternChanged(movementPattern: MovementPattern?) {
        _uiState.update {
            it.copy(
                selectedMovementPattern = movementPattern,
                exercises = filterAndSortExercises(getPredefinedExercises(), it.searchQuery, movementPattern, it.selectedBodyPart)
            )
        }
    }

    fun onBodyPartChanged(bodyPart: BodyPart?) {
        _uiState.update {
            it.copy(
                selectedBodyPart = bodyPart,
                exercises = filterAndSortExercises(getPredefinedExercises(), it.searchQuery, it.selectedMovementPattern, bodyPart)
            )
        }
    }

    private fun filterAndSortExercises(
        exercises: List<Exercise>,
        searchQuery: String,
        movementPattern: MovementPattern?,
        bodyPart: BodyPart?
    ): List<Exercise> {
        return exercises.filter { exercise ->
            val matchesSearchQuery = if (searchQuery.isNotBlank()) {
                exercise.name.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }
            val matchesMovementPattern = movementPattern?.let { it == exercise.movementPattern } ?: true
            val matchesBodyPart = bodyPart?.let { it == exercise.primaryTarget || exercise.otherTargets.contains(it) } ?: true

            matchesSearchQuery && matchesMovementPattern && matchesBodyPart
        }.sortedBy { it.name }
    }
}
