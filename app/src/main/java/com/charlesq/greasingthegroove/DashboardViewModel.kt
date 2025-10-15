package com.charlesq.greasingthegroove

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charlesq.greasingthegroove.ActiveGoal
import com.charlesq.greasingthegroove.DailySetLog
import com.charlesq.greasingthegroove.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

// This UI state will be exposed to the Composable
data class DashboardUiState(
    val streak: Int = 0,
    val setsCompleted: Int = 0,
    val setsGoal: Int = 0,
    val activeExerciseName: String = "Loading...",
    val isLoading: Boolean = true,
    val currentUser: FirebaseUser? = null,
    val signInResultMessage: String? = null // To hold sign-in result messages
)

open class DashboardViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    open val uiState = _uiState.asStateFlow()

    // Private flows to hold the raw data from Firestore
    private val activeGoalFlow = MutableStateFlow<ActiveGoal?>(null)
    private val exerciseFlow = MutableStateFlow<Exercise?>(null)
    private val dailyLogsFlow = MutableStateFlow<List<DailySetLog>>(emptyList())

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.update { it.copy(currentUser = user, isLoading = user != null) }
            if (user != null) {
                Log.d("DashboardViewModel", "User authenticated: ${user.uid}")
                fetchData(user.uid)
            } else {
                Log.d("DashboardViewModel", "User is not authenticated.")
                activeGoalFlow.value = null
                exerciseFlow.value = null
                dailyLogsFlow.value = emptyList()
            }
        }

        viewModelScope.launch {
            combine(
                activeGoalFlow,
                exerciseFlow,
                dailyLogsFlow
            ) { activeGoal, exercise, dailyLogs ->
                // This block now only transforms data, it doesn't manage loading state
                _uiState.update {
                    it.copy(
                        streak = 0, // Simplified for now
                        setsCompleted = dailyLogs.size,
                        setsGoal = activeGoal?.dailyTargetSets ?: 0,
                        activeExerciseName = exercise?.name ?: "No Active Goal"
                    )
                }
            }
        }
    }

    private fun fetchData(userId: String) {
        firestore.collection("activeGoals")
            .whereEqualTo("userId", userId)
            .orderBy("dateSet", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                // This is the end of the initial load.
                _uiState.update { it.copy(isLoading = false) }

                if (error != null) {
                    Log.w("DashboardViewModel", "Listen failed for active goals.", error)
                    return@addSnapshotListener
                }
                val goal = snapshot?.documents?.firstOrNull()?.toObject(ActiveGoal::class.java)
                activeGoalFlow.value = goal
                Log.d("DashboardViewModel", "Active goal updated: $goal")

                if (goal != null) {
                    fetchExercise(goal.exerciseId)
                    fetchDailyLogs(userId, goal.exerciseId)
                } else {
                    exerciseFlow.value = null
                    dailyLogsFlow.value = emptyList()
                }
            }
    }

    private fun fetchExercise(exerciseId: String) {
        // ... (fetchExercise method remains the same)
    }

    private fun fetchDailyLogs(userId: String, exerciseId: String) {
        // ... (fetchDailyLogs method remains the same)
    }

    open fun logSet() {
        // ... (logSet method remains the same)
    }

    fun signInWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _uiState.update { it.copy(signInResultMessage = "Successfully signed in!") }
                Log.d("DashboardViewModel", "Successfully signed in with Google.")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error signing in with Google.", e)
                _uiState.update { it.copy(signInResultMessage = "Failed to sign in: ${e.message}") }
            }
        }
    }

    open fun signOut() {
        auth.signOut()
    }

    fun onSignInFailed(errorMessage: String) {
         _uiState.update { it.copy(signInResultMessage = errorMessage) }
    }

    fun clearSignInResultMessage() {
        _uiState.update { it.copy(signInResultMessage = null) }
    }
}
