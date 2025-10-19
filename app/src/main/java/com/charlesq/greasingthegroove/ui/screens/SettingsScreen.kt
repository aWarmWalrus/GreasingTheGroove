package com.charlesq.greasingthegroove.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.SettingsViewModel
import com.charlesq.greasingthegroove.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dashboardViewModel: DashboardViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    val theme by settingsViewModel.theme.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var tempWeightUnit by remember(dashboardUiState.weightUnit) { mutableStateOf(dashboardUiState.weightUnit) }
    var tempTheme by remember(theme) { mutableStateOf(theme) }

    val hasChanges = tempWeightUnit != dashboardUiState.weightUnit || tempTheme != theme

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
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }


    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                Button(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            dashboardViewModel.updateWeightUnit(tempWeightUnit)
                            settingsViewModel.setTheme(tempTheme)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Weight Unit",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeightUnit.values().forEach { unit ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = (unit == tempWeightUnit),
                                        onClick = { tempWeightUnit = unit }
                                    )
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (unit == tempWeightUnit),
                                    onClick = { tempWeightUnit = unit }
                                )
                                Text(text = unit.name.lowercase())
                            }
                        }
                    }
                }
                HorizontalDivider()
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val themes = listOf("Light", "Dark", "System")
                        themes.forEach { themeValue ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = (themeValue == tempTheme),
                                        onClick = { tempTheme = themeValue }
                                    )
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (themeValue == tempTheme),
                                    onClick = { tempTheme = themeValue }
                                )
                                Text(text = themeValue)
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
            Button(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Text("Sign Out")
            }
        }
    }
}
