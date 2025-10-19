package com.charlesq.greasingthegroove.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise
import com.charlesq.greasingthegroove.ui.movementPatternColors
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseConfigurationScreen(
    navBackStackEntry: NavBackStackEntry,
    onNavigateBack: () -> Unit,
    onNavigateToExercisePicker: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedStateHandle = navBackStackEntry.savedStateHandle

    val mapSaver = Saver<Map<Int, String?>, List<Pair<Int, String?>>>(
        save = { it.toList() },
        restore = { it.toMap() }
    )

    var tempQuickLogExercises by rememberSaveable(stateSaver = mapSaver) { mutableStateOf(uiState.quickLogExercises) }
    var userHasMadeEdits by rememberSaveable { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.quickLogExercises) {
        if (!userHasMadeEdits) {
            tempQuickLogExercises = uiState.quickLogExercises
        }
    }

    val hasChanges = tempQuickLogExercises != uiState.quickLogExercises

    fun handleBackPress() {
        if (hasChanges) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = { handleBackPress() })

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. What would you like to do?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateQuickLogExercises(tempQuickLogExercises.entries.mapNotNull { (k, v) -> v?.let { k to it } }.toMap())
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            }
        )
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle.getStateFlow<String?>("selectedExerciseId", null).collect { exerciseId ->
            val slotIndex = savedStateHandle.get<Int>("slotIndex")
            if (exerciseId != null && slotIndex != null) {
                userHasMadeEdits = true
                val updatedMap = tempQuickLogExercises.toMutableMap()
                updatedMap[slotIndex] = exerciseId
                tempQuickLogExercises = updatedMap
                savedStateHandle.remove<String>("selectedExerciseId")
                savedStateHandle.remove<Int>("slotIndex")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Quick Log") },
                navigationIcon = {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateQuickLogExercises(tempQuickLogExercises.entries.mapNotNull { (k, v) -> v?.let { k to it } }.toMap())
                            onNavigateBack()
                        },
                        enabled = hasChanges
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Select a triangle to configure", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            HexagonLayout(modifier = Modifier.fillMaxWidth()) {
                for (i in 0..5) {
                    val exercise = uiState.predefinedExercises.find { it.id == tempQuickLogExercises[i] }
                    val isModified = tempQuickLogExercises[i] != uiState.quickLogExercises[i]
                    TriangleSlot(
                        exercise = exercise,
                        onClick = { onNavigateToExercisePicker(i) },
                        isModified = isModified,
                        rotation = (i * 60f) + 60f
                    )
                }
            }
        }
    }
}

class RoundedTriangleShape(private val rotation: Float, private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        val vertices = (0..2).map {
            val angle = Math.toRadians(rotation.toDouble() - 90 + 120 * it).toFloat()
            Offset(center.x + radius * cos(angle), center.y + radius * sin(angle))
        }

        val cornerVec = (vertices[0] - vertices[1]).let { it / it.getDistance() } * cornerRadius
        val p1_1 = vertices[0] - cornerVec
        val p1_2 = vertices[1] + cornerVec
        val p2_1 = vertices[1] - (vertices[1] - vertices[2]).let { it / it.getDistance() } * cornerRadius
        val p2_2 = vertices[2] + (vertices[1] - vertices[2]).let { it / it.getDistance() } * cornerRadius
        val p3_1 = vertices[2] - (vertices[2] - vertices[0]).let { it / it.getDistance() } * cornerRadius
        val p3_2 = vertices[0] + (vertices[2] - vertices[0]).let { it / it.getDistance() } * cornerRadius

        path.moveTo(p1_1.x, p1_1.y)
        path.quadraticBezierTo(vertices[0].x, vertices[0].y, p3_2.x, p3_2.y)
        path.lineTo(p3_1.x, p3_1.y)
        path.quadraticBezierTo(vertices[2].x, vertices[2].y, p2_2.x, p2_2.y)
        path.lineTo(p2_1.x, p2_1.y)
        path.quadraticBezierTo(vertices[1].x, vertices[1].y, p1_2.x, p1_2.y)
        path.close()

        return Outline.Generic(path)
    }
}

@Composable
fun TriangleSlot(
    exercise: Exercise?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isModified: Boolean,
    rotation: Float
) {
    val borderColor = exercise?.movementPattern?.let { movementPatternColors[it] } ?: Color.Gray
    val containerColor = if (isModified) borderColor else MaterialTheme.colorScheme.surface
    val textColor = if (isModified) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = RoundedTriangleShape(rotation, 60f)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatExerciseName(exercise?.name ?: "Empty"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun HexagonLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
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