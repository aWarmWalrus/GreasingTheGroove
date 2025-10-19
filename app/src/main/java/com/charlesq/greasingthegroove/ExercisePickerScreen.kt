package com.charlesq.greasingthegroove

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    navController: NavController,
    slotIndex: Int,
    viewModel: ExercisePickerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    options = MovementPattern.values().map { it.name.replace('_', ' ') },
                    selectedOption = uiState.selectedMovementPattern?.name?.replace('_', ' '),
                    onOptionSelected = {
                        val movementPattern = if (it != null) MovementPattern.valueOf(it.replace(' ', '_')) else null
                        viewModel.onMovementPatternChanged(movementPattern)
                    },
                    label = "Movement",
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    options = BodyPart.values().map { it.name.replace('_', ' ') },
                    selectedOption = uiState.selectedBodyPart?.name?.replace('_', ' '),
                    onOptionSelected = {
                        val bodyPart = if (it != null) BodyPart.valueOf(it.replace(' ', '_')) else null
                        viewModel.onBodyPartChanged(bodyPart)
                    },
                    label = "Body Part",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.exercises) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedExerciseId", exercise.id)
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("slotIndex", slotIndex)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption ?: label)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = {
            Text(
                "Movement: ${exercise.movementPattern?.name?.replace('_', ' ')}, Target: ${exercise.primaryTarget?.name?.replace('_', ' ')}"
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
