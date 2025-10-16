package com.charlesq.greasingthegroove

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExercisePickerUiState(
    val exercisesByMetric: Map<MetricType, List<Exercise>> = emptyMap(),
    val expandedMetric: MetricType? = null
)

class ExercisePickerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExercisePickerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        val exercises = getPredefinedExercises()
        val sortedExercisesByMetric = exercises
            .groupBy { it.metric }
            .mapValues { (_, exerciseList) ->
                exerciseList.sortedBy { it.name }
            }
        _uiState.update {
            it.copy(exercisesByMetric = sortedExercisesByMetric)
        }
    }

    fun onMetricClicked(metric: MetricType) {
        _uiState.update {
            if (it.expandedMetric == metric) {
                it.copy(expandedMetric = null) // Collapse if already expanded
            } else {
                it.copy(expandedMetric = metric) // Expand new one
            }
        }
    }
}
