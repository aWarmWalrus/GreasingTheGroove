package com.charlesq.greasingthegroove.ui.dialogs

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.CompletedSet
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise
import com.charlesq.greasingthegroove.WeightUnit
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun DailyLogDialog(
    dashboardViewModel: DashboardViewModel,
    dialogViewModel: DailyLogViewModel = viewModel()
) {
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    val dialogUiState by dialogViewModel.uiState.collectAsState()
    val selectedDate = dashboardUiState.selectedDate
    val setsForDate = selectedDate?.let { dashboardUiState.completedSetsByDate[it] } ?: emptyList()
    val predefinedExercises = dashboardUiState.predefinedExercises.associateBy { it.id }
    val weightUnit = dashboardUiState.weightUnit

    LaunchedEffect(setsForDate, predefinedExercises) {
        dialogViewModel.processSets(setsForDate, predefinedExercises)
    }

    if (dialogUiState.setToBeDeleted != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                dialogUiState.setToBeDeleted?.let {
                    dashboardViewModel.deleteSet(it)
                }
                dialogViewModel.onDeletionConfirmed()
            },
            onDismiss = { dialogViewModel.onDeletionCancelled() }
        )
    }

    if (selectedDate != null) {
        AlertDialog(
            onDismissRequest = {
                Log.d("DailyLogDialog", "Dialog dismissed")
                dashboardViewModel.dismissDailyLogDialog()
            },
            title = {
                val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                Text("Logs for $formattedDate")
            },
            text = {
                if (setsForDate.isEmpty()) {
                    Text("No sets logged for this day.")
                } else {
                    LazyColumn {
                        dialogUiState.setsByExercise.forEach { (exercise, sets) ->
                            item {
                                exercise?.let {
                                    val stats = dialogUiState.exerciseStats[it.id]
                                    ExpandableLogHeader(
                                        exercise = it,
                                        stats = stats,
                                        isExpanded = dialogUiState.expandedExerciseId == it.id,
                                        onClick = { dialogViewModel.onExerciseClicked(it.id) }
                                    )
                                }
                            }
                            item {
                                AnimatedVisibility(
                                    visible = dialogUiState.expandedExerciseId == exercise?.id,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                        sets.forEach { set ->
                                            LogSetItem(
                                                set = set,
                                                weightUnit = weightUnit,
                                                onDelete = { dialogViewModel.onDeletionInitiated(set) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    Log.d("DailyLogDialog", "Close button clicked")
                    dashboardViewModel.dismissDailyLogDialog()
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ExpandableLogHeader(
    exercise: Exercise,
    stats: ExerciseStats?,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (stats != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sets: ${stats.totalSets}", style = MaterialTheme.typography.bodySmall)
                        stats.totalReps?.let {
                            Text("Reps: $it", style = MaterialTheme.typography.bodySmall)
                        }
                        stats.totalDuration?.let {
                            Text("Duration: ${it}s", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSetItem(set: CompletedSet, weightUnit: WeightUnit, onDelete: () -> Unit) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * .25f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            scope.launch {
                dismissState.reset()
            }
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (set.reps != null) {
                        Text("Reps: ${set.reps}", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (set.durationSeconds != null) {
                        Text("Duration: ${set.durationSeconds}s", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (set.weightAdded != null) {
                        val displayWeight = if (weightUnit == WeightUnit.KG) {
                            set.weightAdded / 2.20462
                        } else {
                            set.weightAdded
                        }
                        Text(
                            "Weight: ${String.format("%.1f", displayWeight)} ${weightUnit.name.lowercase()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                set.userCompletedAt?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "at $it",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete this set? This action cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
