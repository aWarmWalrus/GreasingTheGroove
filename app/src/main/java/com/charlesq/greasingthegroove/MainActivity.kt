package com.charlesq.greasingthegroove

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charlesq.greasingthegroove.ui.theme.GreasingTheGrooveTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// --- Data Models are now defined in their own files ---

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
                CommonStatusCodes.NETWORK_ERROR -> "Sign-in failed due to a network error. Please check your connection."
                CommonStatusCodes.CANCELED -> "Sign-in was cancelled."
                CommonStatusCodes.DEVELOPER_ERROR -> "Sign-in failed due to a developer error. Please check the configuration."
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.currentUser == null) {
            WelcomeScreen(onSignInClick = onSignInClick)
        } else {
            DashboardScreen(viewModel = viewModel)
        }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Button(onClick = { viewModel.signOut() }) {
                    Text("Sign Out")
                }
            }
            item {
                StreakDisplay(streak = uiState.streak)
            }
            item {
                ActiveExerciseCard(
                    exerciseName = uiState.activeExerciseName,
                    setsCompleted = uiState.setsCompleted,
                    setsGoal = uiState.setsGoal,
                    onLogSet = { viewModel.logSet() }
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

            GoalProgress(completed = setsCompleted, total = setsGoal)

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
            style = MaterialTheme. typography.bodyLarge
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
            .aspectRatio(1f),
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
