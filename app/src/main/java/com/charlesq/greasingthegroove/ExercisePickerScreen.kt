package com.charlesq.greasingthegroove

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.MetricType

@Composable
fun ExercisePickerScreen(
    onExerciseSelected: (String) -> Unit,
    viewModel: ExercisePickerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                Text(
                    "Select an Exercise",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            uiState.exercisesByMetric.forEach { (metric, exercises) ->
                item {
                    ExpandableExerciseHeader(
                        metric = metric,
                        isExpanded = uiState.expandedMetric == metric,
                        onClick = { viewModel.onMetricClicked(metric) }
                    )
                }
                item {
                    AnimatedVisibility(
                        visible = uiState.expandedMetric == metric,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            exercises.forEach { exercise ->
                                ExerciseListItem(
                                    exercise = exercise,
                                    onClick = { onExerciseSelected(exercise.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableExerciseHeader(
    metric: MetricType,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(metric.displayName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
        Divider(modifier = Modifier.padding(top = 16.dp))
    }
}
