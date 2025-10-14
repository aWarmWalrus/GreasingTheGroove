package com.charlesq.greasingthegroove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlesq.greasingthegroove.ui.theme.GreasingTheGrooveTheme
import java.util.Date

// --- Data Models based on PRD Section 4.2 ---

// Represents the static definition of an exercise.
// From Firestore Collection: /exercises
data class Exercise(
    val id: String = "",
    val name: String = "",
    val exerciseMode: String = "REPS", // 'REPS' or 'DURATION'
    val isCustom: Boolean = false,
    val lastReps: Int? = null,
    val lastWeightAdded: Double? = null,
    val lastDurationSeconds: Int? = null,
    val dateCreated: Date = Date()
)

// Represents the user's active daily goal for a specific exercise.
// From Firestore Collection: /activeGoals
data class ActiveGoal(
    val exerciseId: String = "",
    val dailyTargetSets: Int? = null,
    val dailyTargetDurationSeconds: Int? = null,
    val dateSet: Date = Date()
)

// Represents a single logged set or duration.
// From Firestore Collection: /dailySetLogs
data class DailySetLog(
    val id: String = "",
    val exerciseId: String = "",
    val date: String = "", // YYYY-MM-DD
    val timestamp: Date = Date(),
    val weightAdded: Double? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val notes: String? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Initialize Firebase Auth and Firestore here.
        // Per PRD 4.1, use the provided global variables for authentication.
        // Example:
        // val appId = typeof __app_id !== 'undefined' ? __app_id : 'default-app-id';
        // val firebaseConfig = JSON.parse(__firebase_config);
        // ... initializeApp(firebaseConfig) ...

        setContent {
            GreasingTheGrooveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This is the main entry point for the app's UI.
                    GreasingTheGrooveApp()
                }
            }
        }
    }
}

@Composable
fun GreasingTheGrooveApp() {
    // For now, we'll directly show the Dashboard.
    // In the future, you could add navigation logic here (e.g., for settings, exercise library).
    DashboardScreen()
}

@Composable
fun DashboardScreen() {
    // TODO: Fetch user data from Firestore here.
    // This would include the active exercise, goal, today's progress, and streak.
    // For now, we'll use mock data.
    val currentStreak = 14 // Mock data
    val setsCompleted = 6
    val setsGoal = 10
    val activeExerciseName = "Push-ups"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // FR-3.3.1: Streak Visualization
            StreakDisplay(streak = currentStreak)
        }
        item {
            // This card will contain the primary logging action and progress.
            ActiveExerciseCard(
                exerciseName = activeExerciseName,
                setsCompleted = setsCompleted,
                setsGoal = setsGoal,
                onLogSet = {
                    // This lambda would be triggered to log a set.
                    println("Log button clicked!")
                    // TODO: Implement Firestore write operation for FR-3.2.1
                }
            )
        }
        item {
            // FR-3.3.2: Homepage Calendar View
            ConsistencyCalendar()
        }
        item {
            // FR-3.3.4: Customizable Progress Graphs
            // Placeholder for graphs. This would be a list of user-configured graphs.
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

/**
 * FR-3.3.1: Prominently displays the user's current active streak.
 */
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

/**
 * Combines the quick logging action (FR-3.2.1) and progress feedback (FR-3.2.5).
 */
@Composable
fun ActiveExerciseCard(
    exerciseName: String,
    setsCompleted: Int,
    setsGoal: Int,
    onLogSet: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Today's Goal: $exerciseName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Progress Indicator
            GoalProgress(completed = setsCompleted, total = setsGoal)

            // FR-3.2.1: Primary Logging Action
            Button(
                onClick = onLogSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Log One Set", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun GoalProgress(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$completed / $total sets completed",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * FR-3.3.2: A placeholder for the interactive calendar view.
 */
@Composable
fun ConsistencyCalendar() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Consistency",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Calendar View (W-I-P)",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GreasingTheGrooveTheme {
        DashboardScreen()
    }
}
