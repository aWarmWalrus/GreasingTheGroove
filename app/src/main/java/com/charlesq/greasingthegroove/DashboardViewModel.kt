package com.charlesq.greasingthegroove

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class DashboardUiState(
    val streak: Int = 0,
    val setsCompleted: Int = 0,
    val goalProgress: Int = 0,
    val goalTotal: Int = 0,
    val goalUnits: String = "sets",
    val activeExerciseName: String = "Loading...",
    val isLoading: Boolean = true,
    val currentUser: FirebaseUser? = null,
    val signInResultMessage: String? = null,
    val predefinedExercises: List<Exercise> = emptyList(),
    val activeGoal: ActiveGoal? = null,
    val showLogSetDialog: Boolean = false
)

open class DashboardViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    open val uiState = _uiState.asStateFlow()

    private val activeGoalFlow = MutableStateFlow<ActiveGoal?>(null)
    private val exerciseFlow = MutableStateFlow<Exercise?>(null)
    private val dailyLogsFlow = MutableStateFlow<List<DailySetLog>>(emptyList())

    init {
        loadPredefinedExercises()

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.update { it.copy(currentUser = user) }
            if (user != null) {
                fetchData(user.uid)
            } else {
                activeGoalFlow.value = null
                exerciseFlow.value = null
                dailyLogsFlow.value = emptyList()
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            combine(
                activeGoalFlow,
                exerciseFlow,
                dailyLogsFlow
            ) { activeGoal, exercise, dailyLogs ->
                val goalTotal = activeGoal?.targetValue ?: 0
                val (goalProgress, goalUnits) = when (activeGoal?.targetType) {
                    "REPS" -> dailyLogs.sumOf { it.reps ?: 0 } to "reps"
                    "SECONDS" -> dailyLogs.sumOf { it.durationSeconds ?: 0 } to "seconds"
                    "MINUTES" -> (dailyLogs.sumOf { it.durationSeconds ?: 0 } / 60) to "minutes"
                    else -> dailyLogs.size to "sets"
                }

                _uiState.value.copy(
                    streak = 0,
                    setsCompleted = dailyLogs.size,
                    goalProgress = goalProgress,
                    goalTotal = goalTotal,
                    goalUnits = goalUnits,
                    activeExerciseName = exercise?.name ?: "No Active Goal",
                    activeGoal = activeGoal
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun loadPredefinedExercises() {
        _uiState.update { it.copy(predefinedExercises = getPredefinedExercises()) }
    }

    private fun fetchData(userId: String) {
        _uiState.update { it.copy(isLoading = true) }
        firestore.collection("activeGoals")
            .whereEqualTo("userId", userId)
            .orderBy("dateSet", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                _uiState.update { it.copy(isLoading = false) }

                if (error != null) {
                    Log.w("DashboardViewModel", "Listen failed for active goals.", error)
                    return@addSnapshotListener
                }
                val goal = snapshot?.documents?.firstOrNull()?.toObject(ActiveGoal::class.java)
                activeGoalFlow.value = goal

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
        if (exerciseId.isBlank()) {
            exerciseFlow.value = null
            return
        }
        val predefined = _uiState.value.predefinedExercises.find { it.id == exerciseId }
        if (predefined != null) {
            exerciseFlow.value = predefined
        } else {
            firestore.collection("exercises").document(exerciseId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("DashboardViewModel", "Listen failed for custom exercise.", error)
                        return@addSnapshotListener
                    }
                    exerciseFlow.value = snapshot?.toObject(Exercise::class.java)
                }
        }
    }

    private fun fetchDailyLogs(userId: String, exerciseId: String) {
        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        firestore.collection("dailySetLogs")
            .whereEqualTo("userId", userId)
            .whereEqualTo("exerciseId", exerciseId)
            .whereEqualTo("date", today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("DashboardViewModel", "Listen failed for daily logs.", error)
                    return@addSnapshotListener
                }
                dailyLogsFlow.value = snapshot?.toObjects(DailySetLog::class.java) ?: emptyList()
            }
    }
    
    fun onLogSetClicked() {
        val goal = _uiState.value.activeGoal
        if (goal?.targetType == "SETS") {
            saveLog()
        } else {
            _uiState.update { it.copy(showLogSetDialog = true) }
        }
    }

    fun dismissLogSetDialog() {
        _uiState.update { it.copy(showLogSetDialog = false) }
    }

    fun saveLog(reps: Int? = null, durationSeconds: Int? = null, weightAdded: Double? = null, userCompletedAt: String? = null) {
        val userId = auth.currentUser?.uid
        val goal = activeGoalFlow.value
        if (userId == null || goal == null) return

        viewModelScope.launch {
            try {
                val newLog = DailySetLog(
                    userId = userId,
                    exerciseId = goal.exerciseId,
                    date = LocalDate.now(ZoneId.systemDefault()).toString(),
                    reps = reps,
                    durationSeconds = durationSeconds,
                    weightAdded = weightAdded,
                    userCompletedAt = userCompletedAt?.ifBlank { null },
                    timestamp = null
                )
                firestore.collection("dailySetLogs").add(newLog).await()
                dismissLogSetDialog()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error logging set", e)
            }
        }
    }
    
    fun createGoal(
        exerciseId: String,
        goalFrequency: String,
        targetType: String,
        targetValue: Int
    ) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val newGoal = ActiveGoal(
                    userId = userId,
                    exerciseId = exerciseId,
                    goalFrequency = goalFrequency,
                    targetType = targetType,
                    targetValue = targetValue,
                    dateSet = null
                )
                firestore.collection("activeGoals").add(newGoal).await()
                Log.d("DashboardViewModel", "Successfully created new goal.")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error creating goal", e)
            }
        }
    }

    fun signInWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _uiState.update { it.copy(signInResultMessage = "Successfully signed in!") }
            } catch (e: Exception) {
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
