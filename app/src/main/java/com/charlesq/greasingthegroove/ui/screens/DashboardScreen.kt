package com.charlesq.greasingthegroove.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise
import com.charlesq.greasingthegroove.ui.composables.ConsistencyCalendar
import com.charlesq.greasingthegroove.ui.dialogs.DailyLogDialog
import com.charlesq.greasingthegroove.ui.dialogs.LogSetDialog
import com.charlesq.greasingthegroove.ui.theme.JapandiBeige
import com.charlesq.greasingthegroove.ui.theme.JapandiCharcoal
import com.charlesq.greasingthegroove.ui.theme.JapandiLightGray
import com.charlesq.greasingthegroove.ui.theme.JapandiMutedGreen
import com.charlesq.greasingthegroove.ui.theme.getContrastingTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToExerciseConfiguration: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                Log.d("DashboardScreen", "DashboardScreen composed")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (uiState.showLogSetDialog) {
        LogSetDialog(viewModel = viewModel)
    }

    if (uiState.showDailyLogDialog) {
        DailyLogDialog(dashboardViewModel = viewModel)
    }

    val colorMap = mapOf(
        "Squats" to JapandiBeige,
        "Pull-ups" to JapandiMutedGreen,
        "Push-ups" to JapandiLightGray,
        "Plank" to JapandiCharcoal,
        "Dips" to JapandiBeige,
        "Lunges" to JapandiMutedGreen,
        "Calf Raises" to JapandiLightGray,
        "Hanging Leg Raises" to JapandiCharcoal,
        "Wall Sit" to JapandiBeige,
        "Hollow Body Hold" to JapandiMutedGreen,
        "Dead Hang" to JapandiLightGray,
        "L-Sit" to JapandiCharcoal
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = {
                        Log.d("DashboardScreen", "Settings icon clicked")
                        onNavigateToSettings()
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val selectedExercises = uiState.quickLogExercises.values.mapNotNull { exerciseId ->
                uiState.predefinedExercises.find { it.id == exerciseId }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Quick Log",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        IconButton(onClick = {
                            Log.d("DashboardScreen", "Configure button clicked")
                            onNavigateToExerciseConfiguration()
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Configure")
                        }
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            ExerciseGrid(
                                exercises = selectedExercises,
                                onExerciseClicked = { exercise ->
                                    Log.d("DashboardScreen", "Exercise item clicked: ${exercise.name}")
                                    viewModel.onLogSetClicked(exercise)
                                },
                                colorMap = colorMap
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Consistency",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            ConsistencyCalendar(
                                viewModel = viewModel,
                                colorMap = colorMap,
                                predefinedExercises = uiState.predefinedExercises
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseGrid(
    exercises: List<Exercise>,
    onExerciseClicked: (Exercise) -> Unit,
    colorMap: Map<String, Color>
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        exercises.chunked(2).forEach { rowExercises ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowExercises.forEach { exercise ->
                    val bgColor = colorMap[exercise.name] ?: Color.LightGray
                    ExerciseItem(
                        exercise = exercise,
                        onClick = { onExerciseClicked(exercise) },
                        modifier = Modifier.weight(1f),
                        containerColor = bgColor,
                        contentColor = getContrastingTextColor(bgColor)
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier
            .aspectRatio(1.6f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
