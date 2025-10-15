package com.charlesq.greasingthegroove

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.charlesq.greasingthegroove.ui.theme.GreasingTheGrooveTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            viewModel.signInWithGoogleCredential(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("MainActivity", "Google sign in failed", e)
            val errorMessage = when (e.statusCode) {
                CommonStatusCodes.NETWORK_ERROR -> "Sign-in failed due to a network error."
                CommonStatusCodes.CANCELED -> "Sign-in was cancelled."
                CommonStatusCodes.DEVELOPER_ERROR -> "Sign-in failed due to a developer error."
                else -> "An unknown error occurred during sign-in."
            }
            viewModel.onSignInFailed(errorMessage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreasingTheGrooveTheme {
                GreasingTheGrooveApp(
                    viewModel = viewModel,
                    onSignInClick = { startGoogleSignIn() }
                )
            }
        }
    }

    private fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }
}

@Composable
fun GreasingTheGrooveApp(
    viewModel: DashboardViewModel,
    onSignInClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = if (uiState.currentUser == null) "welcome" else "dashboard"
        ) {
            composable("welcome") {
                WelcomeScreen(onSignInClick = onSignInClick)
            }
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToCreateGoal = { navController.navigate("exercise_picker") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("exercise_picker") {
                ExercisePickerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onExerciseSelected = { exerciseId ->
                        navController.navigate("goal_setup/$exerciseId")
                    }
                )
            }
            composable(
                "goal_setup/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                GoalSetupScreen(
                    viewModel = viewModel,
                    exerciseId = exerciseId,
                    onNavigateBack = { navController.popBackStack() },
                    onGoalCreated = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } }
                )
            }
        }
    }

    if (uiState.showLogSetDialog) {
        LogSetDialog(viewModel = viewModel)
    }

    uiState.signInResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSignInResultMessage() },
            title = { Text("Sign-In Result") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { viewModel.clearSignInResultMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun WelcomeScreen(onSignInClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Long term consistency trumps short term intensity",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(onClick = onSignInClick) {
            Text("Sign in with Google")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateGoal: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    StreakDisplay(streak = uiState.streak)
                }
                item {
                    ActiveExerciseCard(
                        exerciseName = uiState.activeExerciseName,
                        progress = uiState.goalProgress,
                        total = uiState.goalTotal,
                        units = uiState.goalUnits,
                        onLogSet = { viewModel.onLogSetClicked() },
                        hasActiveGoal = uiState.activeGoal != null,
                        onCreateGoal = onNavigateToCreateGoal
                    )
                }
                item {
                    ConsistencyCalendar()
                }
                item {
                    Text(
                        text = "Progress Graphs",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Graph for Max Weight Added will go here.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveExerciseCard(
    exerciseName: String,
    progress: Int,
    total: Int,
    units: String,
    onLogSet: () -> Unit,
    hasActiveGoal: Boolean,
    onCreateGoal: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (hasActiveGoal) {
                Text(
                    text = "Today's Goal: $exerciseName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                GoalProgress(completed = progress, total = total, units = units)
                Button(onClick = onLogSet, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Log One Set", fontSize = 18.sp)
                }
            } else {
                Text(
                    text = "No Active Goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onCreateGoal, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Create a Goal", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun GoalProgress(completed: Int, total: Int, units: String) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$completed / $total $units completed",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit,
    onExerciseSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val exercises = uiState.predefinedExercises.groupBy { it.exerciseMode }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose an Exercise") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            exercises.forEach { (mode, exerciseList) ->
                item {
                    ExerciseCategorySection(
                        title = mode,
                        exercises = exerciseList,
                        onExerciseSelected = onExerciseSelected
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCategorySection(
    title: String,
    exercises: List<Exercise>,
    onExerciseSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ListItem(
            headlineContent = { Text(title, style = MaterialTheme.typography.titleLarge) },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            },
            modifier = Modifier.clickable { expanded = !expanded }
        )

        AnimatedVisibility(visible = expanded) {
            Column {
                exercises.forEach { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        modifier = Modifier
                            .clickable { onExerciseSelected(exercise.id) }
                            .padding(start = 16.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupScreen(
    viewModel: DashboardViewModel,
    exerciseId: String,
    onNavigateBack: () -> Unit,
    onGoalCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val exercise = uiState.predefinedExercises.find { it.id == exerciseId }
    val focusManager = LocalFocusManager.current

    var selectedTimeFrame by remember { mutableStateOf<String?>(null) }
    var selectedTargetType by remember { mutableStateOf<String?>(null) }
    var targetValue by remember { mutableStateOf("") }

    val isFormValid = selectedTimeFrame != null && selectedTargetType != null && targetValue.toIntOrNull() != null
    
    val onSave = {
        if(isFormValid) {
            viewModel.createGoal(
                exerciseId = exerciseId,
                goalFrequency = selectedTimeFrame!!,
                targetType = selectedTargetType!!,
                targetValue = targetValue.toIntOrNull() ?: 0
            )
            onGoalCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Goal for ${exercise?.name}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Step 1: Time Frame
            Text("Select a time frame for your goal:", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (selectedTimeFrame == "DAILY") {
                    Button(onClick = { selectedTimeFrame = "DAILY" }) { Text("Daily") }
                } else {
                    FilledTonalButton(onClick = { selectedTimeFrame = "DAILY" }) { Text("Daily") }
                }
                if (selectedTimeFrame == "WEEKLY") {
                    Button(onClick = { selectedTimeFrame = "WEEKLY" }) { Text("Weekly") }
                } else {
                    FilledTonalButton(onClick = { selectedTimeFrame = "WEEKLY" }) { Text("Weekly") }
                }
            }

            // Step 2: Target Type
            AnimatedVisibility(visible = selectedTimeFrame != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select a target type:", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (exercise?.exerciseMode == "REPS") {
                            if (selectedTargetType == "REPS") {
                                Button(onClick = { selectedTargetType = "REPS" }) { Text("Reps") }
                            } else {
                                FilledTonalButton(onClick = { selectedTargetType = "REPS" }) { Text("Reps") }
                            }
                        } else { // ISOMETRICS
                             if (selectedTargetType == "SECONDS") {
                                Button(onClick = { selectedTargetType = "SECONDS" }) { Text("Seconds") }
                            } else {
                                FilledTonalButton(onClick = { selectedTargetType = "SECONDS" }) { Text("Seconds") }
                            }
                             if (selectedTargetType == "MINUTES") {
                                Button(onClick = { selectedTargetType = "MINUTES" }) { Text("Minutes") }
                            } else {
                                FilledTonalButton(onClick = { selectedTargetType = "MINUTES" }) { Text("Minutes") }
                            }
                        }
                        if (selectedTargetType == "SETS") {
                            Button(onClick = { selectedTargetType = "SETS" }) { Text("Total Sets") }
                        } else {
                            FilledTonalButton(onClick = { selectedTargetType = "SETS" }) { Text("Total Sets") }
                        }
                    }
                }
            }

            // Step 3: Target Value
            AnimatedVisibility(visible = selectedTargetType != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Enter your target value:", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it },
                        label = { Text("Target ${selectedTargetType?.lowercase() ?: ""}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onSave()
                            }
                        )
                    )
                }
            }

            // Save Button
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onSave,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Goal")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSetDialog(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val exercise = uiState.predefinedExercises.find { it.id == uiState.activeGoal?.exerciseId }

    var reps by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    
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

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
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
        title = { Text("Log a Set for ${exercise?.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (exercise?.exerciseMode == "REPS") {
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                            if (!isTimerRunning) { // If timer was just paused
                                duration = timerValue.toString()
                            }
                        }) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) "Pause" else "Start",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        TextButton(onClick = {
                            isTimerRunning = false
                            timerValue = 0L
                            duration = ""
                        }) { Text("Reset") }
                    }
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (seconds)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight Added (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
            Button(onClick = {
                viewModel.saveLog(
                    reps = reps.toIntOrNull(),
                    durationSeconds = duration.toIntOrNull(),
                    weightAdded = weight.toDoubleOrNull(),
                    userCompletedAt = timeCompleted
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.dismissLogSetDialog() }) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { viewModel.signOut() }) {
                Text("Sign Out")
            }
        }
    }
}


@Composable
fun StreakDisplay(streak: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$streak",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Day Streak",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun ConsistencyCalendar() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Consistency",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                val month = state.firstVisibleMonth.yearMonth
                val monthTitle = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year
                Text(
                    text = monthTitle,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalCalendar(
                    state = state,
                    dayContent = { day -> Day(day) },
                )
            }
        }
    }
}

@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f), // This is important for square-sizing!
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
        )
    }
}

@SuppressLint("RestrictedApi")
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GreasingTheGrooveTheme {
        WelcomeScreen(onSignInClick = {})
    }
}
