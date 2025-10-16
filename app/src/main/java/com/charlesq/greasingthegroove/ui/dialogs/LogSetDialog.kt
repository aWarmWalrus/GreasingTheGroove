package com.charlesq.greasingthegroove.ui.dialogs

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.MetricType
import com.charlesq.greasingthegroove.WeightUnit
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSetDialog(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val exercise = uiState.selectedExercise
    val lastWeight = uiState.selectedExerciseLastWeight
    val weightUnit = uiState.weightUnit
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        Log.d("LogSetDialog", "LogSetDialog composed")
        onDispose {
            Log.d("LogSetDialog", "LogSetDialog disposed")
        }
    }

    var reps by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    LaunchedEffect(lastWeight, weightUnit) {
        if (lastWeight != null && lastWeight > 0) {
            val displayWeight = if (weightUnit == WeightUnit.KG) {
                lastWeight / 2.20462
            } else {
                lastWeight
            }
            weight = String.format("%.1f", displayWeight)
        }
    }

    var repsError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }

    val formatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    var timeCompleted by remember { mutableStateOf(LocalTime.now().format(formatter)) }

    var timerValue by remember { mutableStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute
    )

    LaunchedEffect(key1 = isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            timerValue++
        }
    }

    fun validateReps(value: String) {
        val intValue = value.toIntOrNull()
        repsError = when {
            value.isEmpty() -> "Reps cannot be empty"
            intValue == null -> "Invalid number"
            intValue <= 0 -> "Reps must be positive"
            intValue > 256 -> "Reps cannot exceed 256"
            else -> null
        }
    }

    fun validateDuration(value: String) {
        val intValue = value.toIntOrNull()
        durationError = when {
            value.isEmpty() -> "Duration cannot be empty"
            intValue == null -> "Invalid number"
            intValue <= 0 -> "Duration must be positive"
            intValue > 256 -> "Duration cannot exceed 256"
            else -> null
        }
    }


    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = {
                Log.d("LogSetDialog", "Time picker dismissed")
                showTimePicker = false
            },
            title = { Text("Select Time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    timeCompleted = selectedTime.format(formatter)
                    Log.d("LogSetDialog", "Time set to $timeCompleted")
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = {
                    Log.d("LogSetDialog", "Time picker cancelled")
                    showTimePicker = false
                }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            Log.d("LogSetDialog", "Dialog dismissed")
            viewModel.dismissLogSetDialog()
        },
        title = { Text("Log a Set for ${exercise?.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (exercise?.metric == MetricType.REPS) {
                    OutlinedTextField(
                        value = reps,
                        onValueChange = {
                            reps = it.filter { char -> char.isDigit() }
                            validateReps(reps)
                        },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                        singleLine = true,
                        isError = repsError != null,
                        supportingText = { repsError?.let { Text(it) } }
                    )
                } else { // ISOMETRICS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${timerValue}s",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            isTimerRunning = !isTimerRunning
                            Log.d("LogSetDialog", "Timer running: $isTimerRunning")
                            if (!isTimerRunning) { // If timer was just paused
                                duration = timerValue.toString()
                                validateDuration(duration)
                            }
                        }) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) "Pause" else "Start",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        TextButton(onClick = {
                            Log.d("LogSetDialog", "Timer reset")
                            isTimerRunning = false
                            timerValue = 0L
                            duration = ""
                            validateDuration(duration)
                        }) { Text("Reset") }
                    }
                    OutlinedTextField(
                        value = duration,
                        onValueChange = {
                            duration = it.filter { char -> char.isDigit() }
                            validateDuration(duration)
                        },
                        label = { Text("Duration (seconds)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                        singleLine = true,
                        isError = durationError != null,
                        supportingText = { durationError?.let { Text(it) } }
                    )
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        val regex = Regex("^\\d*\\.?\\d?\$")
                        if (regex.matches(it)) {
                            weight = it
                        }
                    },
                    label = { Text("Weight Added (${weightUnit.name.lowercase()}) (optional)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    singleLine = true
                )
                OutlinedTextField(
                    value = timeCompleted,
                    onValueChange = { /* Handled by clickable */ },
                    label = { Text("Time Completed") },
                    readOnly = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        Log.d("LogSetDialog", "Time completed field clicked")
                                        showTimePicker = true
                                    }
                                }
                            }
                        }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("LogSetDialog", "Save button clicked")
                    if (exercise?.metric == MetricType.REPS) validateReps(reps)
                    else validateDuration(duration)

                    if (repsError == null && durationError == null) {
                        viewModel.saveLog(
                            reps = reps.toIntOrNull(),
                            durationSeconds = duration.toIntOrNull(),
                            weightAdded = weight.toDoubleOrNull(),
                            userCompletedAt = timeCompleted
                        )
                    }
                },
                enabled = repsError == null && durationError == null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = {
                Log.d("LogSetDialog", "Cancel button clicked")
                viewModel.dismissLogSetDialog()
            }) {
                Text("Cancel")
            }
        }
    )
}
