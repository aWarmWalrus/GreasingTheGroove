package com.charlesq.greasingthegroove.ui.dialogs

import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
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
    val setToBeEdited = uiState.setToBeEdited
    val lastWeight = uiState.selectedExerciseLastWeight
    val weightUnit = uiState.weightUnit
    val focusManager = LocalFocusManager.current
    val view = LocalView.current

    var reps by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
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

    LaunchedEffect(setToBeEdited) {
        setToBeEdited?.let { set ->
            reps = set.reps?.toString() ?: ""
            duration = set.durationSeconds?.toString() ?: ""
            val displayWeight = if (weightUnit == WeightUnit.KG) {
                set.weightAdded?.div(2.20462)
            } else {
                set.weightAdded
            }
            weight = displayWeight?.let { String.format("%.1f", it) } ?: ""
            timeCompleted = set.userCompletedAt ?: LocalTime.now().format(formatter)
        }
    }

    DisposableEffect(isTimerRunning) {
        val window = (view.context as? android.app.Activity)?.window
        if (isTimerRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(lastWeight, weightUnit) {
        if (setToBeEdited == null && lastWeight != null && lastWeight > 0) {
            val displayWeight = if (weightUnit == WeightUnit.KG) {
                lastWeight / 2.20462
            } else {
                lastWeight
            }
            weight = String.format("%.1f", displayWeight)
        }
    }

    LaunchedEffect(key1 = isTimerRunning) {
        while (isTimerRunning) {
            delay(10L) // Update every 10 milliseconds for smoother UI
            timerValue += 10
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
        val doubleValue = value.toDoubleOrNull()
        durationError = when {
            value.isEmpty() -> "Duration cannot be empty"
            doubleValue == null -> "Invalid number"
            doubleValue <= 0 -> "Duration must be positive"
            doubleValue > 999 -> "Duration cannot exceed 999"
            else -> null
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
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
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = { viewModel.dismissLogSetDialog() },
        title = { Text(exercise?.name ?: "") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isTimerRunning = !isTimerRunning
                                    if (!isTimerRunning) { // If timer was just paused
                                        duration = String.format("%.2f", timerValue / 1000.0)
                                        validateDuration(duration)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(60.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isTimerRunning) "Pause" else "Start",
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                val seconds = timerValue / 1000
                                val milliseconds = (timerValue % 1000) / 10
                                Text(
                                    text = String.format("%02d", seconds),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(".%02d", milliseconds),
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            IconButton(onClick = {
                                isTimerRunning = false
                                timerValue = 0L
                                duration = ""
                                validateDuration(duration)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Reset"
                                )
                            }

                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duration,
                        onValueChange = {
                            val regex = Regex("^\\d*\\.?\\d{0,2}\$")
                            if (regex.matches(it)) {
                                duration = it
                                validateDuration(duration)
                            }
                        },
                        label = { Text("Duration (seconds)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
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
                    if (exercise?.metric == MetricType.REPS) validateReps(reps)
                    else validateDuration(duration)

                    if (repsError == null && durationError == null) {
                        if (setToBeEdited == null) {
                            viewModel.saveLog(
                                reps = reps.toIntOrNull(),
                                durationSeconds = duration.toDoubleOrNull(),
                                weightAdded = weight.toDoubleOrNull(),
                                userCompletedAt = timeCompleted
                            )
                        } else {
                            viewModel.updateLog(
                                reps = reps.toIntOrNull(),
                                durationSeconds = duration.toDoubleOrNull(),
                                weightAdded = weight.toDoubleOrNull(),
                                userCompletedAt = timeCompleted
                            )
                        }
                    }
                },
                enabled = repsError == null && durationError == null
            ) {
                Text(if (setToBeEdited == null) "Save" else "Update")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.dismissLogSetDialog() }) {
                Text("Cancel")
            }
        }
    )
}
