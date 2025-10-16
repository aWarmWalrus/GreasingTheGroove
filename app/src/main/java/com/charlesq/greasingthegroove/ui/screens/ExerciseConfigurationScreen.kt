package com.charlesq.greasingthegroove.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise

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
    var tempQuickLogExercises by remember(uiState.quickLogExercises) { mutableStateOf(uiState.quickLogExercises) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    fun handleBackPress() {
        if (tempQuickLogExercises != uiState.quickLogExercises) {
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
                        viewModel.updateQuickLogExercises(tempQuickLogExercises)
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateQuickLogExercises(tempQuickLogExercises)
                        onNavigateBack()
                    }) {
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
            Text("Select a square to configure", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in 0..1) {
                    val exercise = uiState.predefinedExercises.find { it.id == tempQuickLogExercises[i] }
                    val isModified = tempQuickLogExercises[i] != uiState.quickLogExercises[i]
                    ExerciseSlot(
                        exercise = exercise,
                        onClick = { onNavigateToExercisePicker(i) },
                        modifier = Modifier.weight(1f),
                        isModified = isModified
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in 2..3) {
                    val exercise = uiState.predefinedExercises.find { it.id == tempQuickLogExercises[i] }
                    val isModified = tempQuickLogExercises[i] != uiState.quickLogExercises[i]
                    ExerciseSlot(
                        exercise = exercise,
                        onClick = { onNavigateToExercisePicker(i) },
                        modifier = Modifier.weight(1f),
                        isModified = isModified
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseSlot(
    exercise: Exercise?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isModified: Boolean
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = if (isModified) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(exercise?.name ?: "Empty")
        }
    }
}
