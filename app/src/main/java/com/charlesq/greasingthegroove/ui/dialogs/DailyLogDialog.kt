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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.CompletedSet
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise
import com.charlesq.greasingthegroove.WeightUnit
import java.time.format.DateTimeFormatter
import kotlin.math.floor

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

    if (dialogUiState.setToBeEdited != null) {
        dialogUiState.setToBeEdited?.let {
            dashboardViewModel.onLogSetClicked(
                exercise = predefinedExercises[it.exerciseId],
                set = it
            )
        }
        dialogViewModel.onEditConfirmed()
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
                                                onDelete = { dialogViewModel.onDeletionInitiated(set) },
                                                onEdit = { dialogViewModel.onEditInitiated(set) }
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
                            Text("Duration: ${formatDuration(it)}", style = MaterialTheme.typography.bodySmall)
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

fun formatDuration(duration: Double): String {
    return if (duration < 120) {
        "${duration.toInt()}s"
    } else {
        val minutes = floor(duration / 60).toInt()
        val seconds = (duration % 60).toInt()
        "${minutes}m ${seconds}s"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSetItem(set: CompletedSet, weightUnit: WeightUnit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> onEdit()
                SwipeToDismissBoxValue.Settled -> {}
            }
            false
        },
        positionalThreshold = { it * 0.75f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(vertical = 4.dp),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> lerp(
                    start = Color.Transparent,
                    stop = Color.Red,
                    fraction = dismissState.progress
                )
                SwipeToDismissBoxValue.StartToEnd -> lerp(
                    start = Color.Transparent,
                    stop = Color.Green,
                    fraction = dismissState.progress
                )
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = CardDefaults.shape)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        it,
                        contentDescription = "Action",
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                        Text("Duration: ${String.format("%.1f", set.durationSeconds)}s", style = MaterialTheme.typography.bodyLarge)
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
