package com.charlesq.greasingthegroove.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise
import com.charlesq.greasingthegroove.MovementPattern
import com.charlesq.greasingthegroove.ui.composables.ConsistencyCalendar
import com.charlesq.greasingthegroove.ui.dialogs.DailyLogDialog
import com.charlesq.greasingthegroove.ui.dialogs.LogSetDialog
import com.charlesq.greasingthegroove.ui.movementPatternColors
import com.charlesq.greasingthegroove.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

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
                        Box(modifier = Modifier.padding(0.dp)) {
                            HexagonQuickLogLayout(
                                exercises = selectedExercises,
                                onExerciseClicked = { exercise ->
                                    Log.d("DashboardScreen", "Exercise item clicked: ${exercise.name}")
                                    viewModel.onLogSetClicked(exercise)
                                },
                                colorMap = movementPatternColors
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            ConsistencyCalendar(
                                viewModel = viewModel,
                                colorMap = movementPatternColors,
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
fun HexagonQuickLogLayout(
    exercises: List<Exercise>,
    onExerciseClicked: (Exercise) -> Unit,
    colorMap: Map<MovementPattern, Color>
) {
    Layout(
        content = {
            exercises.forEachIndexed { index, exercise ->
                TriangleQuickLogItem(
                    exercise = exercise,
                    onClick = { onExerciseClicked(exercise) },
                    containerColor = colorMap[exercise.movementPattern] ?: Color.LightGray,
                    contentColor = getContrastingTextColor(colorMap[exercise.movementPattern] ?: Color.LightGray),
                    rotation = (index * 60f) + 60f
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val itemSize = (constraints.maxWidth / 2.05f).toInt()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0, maxWidth = itemSize, maxHeight = itemSize)) }

        val hexagonRadius = itemSize * 0.6f
        val center = Offset(constraints.maxWidth / 2f, constraints.maxWidth / 2f)

        layout(constraints.maxWidth, constraints.maxWidth) {
            placeables.forEachIndexed { index, placeable ->
                val angle = Math.toRadians(60.0 * index + 30.0).toFloat()
                val x = center.x + hexagonRadius * cos(angle) - itemSize / 2
                val y = center.y + hexagonRadius * sin(angle) - itemSize / 2
                placeable.placeRelative(x.toInt(), y.toInt())
            }
        }
    }
}

fun formatExerciseName(name: String): String {
    return if (name.length > 10 && name.contains(" ")) {
        name.replaceFirst(" ", "\n")
    } else {
        name
    }
}

@Composable
fun TriangleQuickLogItem(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    rotation: Float
) {
    val shape = RoundedTriangleShape(rotation, 60f)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (isPressed) 2.dp else 8.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(elevation, shape)
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatExerciseName(exercise.name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 16.sp
        )
    }
}