package com.charlesq.greasingthegroove

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

enum class WeightUnit {
    LB, KG
}

data class DashboardUiState(
    val setsCompletedToday: Int = 0,
    val isLoading: Boolean = true,
    val isAuthenticating: Boolean = true,
    val currentUser: FirebaseUser? = null,
    val signInResultMessage: String? = null,
    val predefinedExercises: List<Exercise> = emptyList(),
    val showLogSetDialog: Boolean = false,
    val showDailyLogDialog: Boolean = false,
    val completedSetsByDate: Map<LocalDate, List<CompletedSet>> = emptyMap(),
    val selectedExercise: Exercise? = null,
    val selectedExerciseLastWeight: Double? = null,
    val lastWeights: Map<String, Double> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val quickLogExercises: Map<Int, String> = mapOf(
        0 to "squats",
        1 to "pull_ups",
        2 to "push_ups",
        3 to "plank"
    ),
    val weightUnit: WeightUnit = WeightUnit.LB
)

open class DashboardViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    open val uiState = _uiState.asStateFlow()

    private val completedSetsFlow = MutableStateFlow<Map<LocalDate, List<CompletedSet>>>(emptyMap())
    private var userPreferencesListener: ListenerRegistration? = null
    private var completedSetsListener: ListenerRegistration? = null
    init {
        Log.d("DashboardViewModel", "ViewModel init. Attaching AuthStateListener.")
        loadPredefinedExercises()

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            Log.d("DashboardViewModel", "AuthStateListener fired. User: ${user?.uid}")
            _uiState.update { it.copy(currentUser = user, isAuthenticating = false) }

            if (user != null) {
                attachDataListeners(user.uid)
            } else {
                Log.d("DashboardViewModel", "User is null, clearing user-specific data.")
                completedSetsFlow.value = emptyMap()
                removeListeners()
            }
        }

        viewModelScope.launch {
            completedSetsFlow.collect { setsMap ->
                val today = LocalDate.now(ZoneId.systemDefault())
                val setsToday = setsMap[today]?.size ?: 0
                _uiState.update {
                    it.copy(
                        completedSetsByDate = setsMap,
                        setsCompletedToday = setsToday,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun attachDataListeners(userId: String) {
        // Remove existing listeners to avoid duplicates
        removeListeners()

        fetchUserPreferences(userId)
        fetchCompletedSetsForMonth(YearMonth.now(), userId)
    }
    private fun loadPredefinedExercises() {
        _uiState.update { it.copy(predefinedExercises = getPredefinedExercises()) }
    }

    private fun fetchUserPreferences(userId: String) {
        userPreferencesListener = firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.w("DashboardViewModel", "Listen failed for user preferences.", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val quickLogExercisesMap = document.get("quickLogExercises") as? Map<String, String>
                    if (quickLogExercisesMap != null) {
                        val intKeyMap = quickLogExercisesMap.mapKeys { it.key.toInt() }
                        _uiState.update { it.copy(quickLogExercises = intKeyMap) }
                    }
                    val weightUnitString = document.getString("weightUnit")
                    if (weightUnitString != null) {
                        _uiState.update { it.copy(weightUnit = WeightUnit.valueOf(weightUnitString)) }
                    }
                } else {
                    Log.d("DashboardViewModel", "Current data: null")
                }
            }
    }

    fun updateQuickLogExercise(slotIndex: Int, exerciseId: String) {
        val updatedMap = _uiState.value.quickLogExercises.toMutableMap()
        updatedMap[slotIndex] = exerciseId
        _uiState.update { it.copy(quickLogExercises = updatedMap) }
        saveUserPreferences()
    }

    fun updateQuickLogExercises(updatedMap: Map<Int, String>) {
        _uiState.update { it.copy(quickLogExercises = updatedMap) }
        saveUserPreferences()
    }


    fun updateWeightUnit(weightUnit: WeightUnit) {
        _uiState.update { it.copy(weightUnit = weightUnit) }
        saveUserPreferences()
    }


    private fun saveUserPreferences() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val stringKeyMap = _uiState.value.quickLogExercises.mapKeys { it.key.toString() }
            val preferences = mapOf(
                "quickLogExercises" to stringKeyMap,
                "weightUnit" to _uiState.value.weightUnit.name
            )
            firestore.collection("users").document(userId)
                .set(preferences, SetOptions.merge())
        }
    }


    fun fetchCompletedSetsForMonth(yearMonth: YearMonth, userId: String) {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()

        completedSetsListener = firestore.collection("dailySetLogs")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("DashboardViewModel", "Listen failed for monthly logs.", error)
                    return@addSnapshotListener
                }
                val sets = snapshot?.toObjects(CompletedSet::class.java) ?: emptyList()
                val setsByDate = sets.groupBy { LocalDate.parse(it.date) }
                completedSetsFlow.value = setsByDate
            }
    }

    fun onLogSetClicked(exercise: Exercise) {
        val lastWeight = _uiState.value.lastWeights[exercise.id]
        _uiState.update { it.copy(showLogSetDialog = true, selectedExercise = exercise, selectedExerciseLastWeight = lastWeight) }
    }

    fun dismissLogSetDialog() {
        _uiState.update { it.copy(showLogSetDialog = false, selectedExercise = null, selectedExerciseLastWeight = null) }
    }

    fun onDayClicked(date: LocalDate) {
        _uiState.update { it.copy(showDailyLogDialog = true, selectedDate = date) }
    }

    fun dismissDailyLogDialog() {
        _uiState.update { it.copy(showDailyLogDialog = false, selectedDate = null) }
    }

    fun saveLog(reps: Int? = null, durationSeconds: Int? = null, weightAdded: Double? = null, userCompletedAt: String? = null) {
        val userId = auth.currentUser?.uid
        val exercise = _uiState.value.selectedExercise
        if (userId == null || exercise == null) {
            Log.e("DashboardViewModel", "Cannot log set, user or exercise is null.")
            return
        }

        var weightInLb = weightAdded
        if (weightAdded != null && _uiState.value.weightUnit == WeightUnit.KG) {
            weightInLb = weightAdded * 2.20462
        }


        if (weightInLb != null) {
            val updatedLastWeights = _uiState.value.lastWeights.toMutableMap()
            updatedLastWeights[exercise.id] = weightInLb
            _uiState.update { it.copy(lastWeights = updatedLastWeights) }
        }

        viewModelScope.launch {
            try {
                val today = LocalDate.now(ZoneId.systemDefault())
                val dateString = today.toString()

                val newLog = CompletedSet(
                    userId = userId,
                    exerciseId = exercise.id,
                    date = dateString,
                    reps = reps,
                    durationSeconds = durationSeconds,
                    weightAdded = weightInLb,
                    userCompletedAt = userCompletedAt?.ifBlank { null },
                    timestamp = null // Firestore will set this
                )

                firestore.collection("dailySetLogs").add(newLog).await()

                dismissLogSetDialog()
                Log.d("DashboardViewModel", "Successfully logged set.")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error in saveLog", e)
            }
        }
    }

    fun signInWithGoogleCredential(idToken: String) {
        Log.d("SignIn", "signInWithGoogleCredential called with idToken: $idToken")
        viewModelScope.launch {
            try {
                Log.d("SignIn", "Calling GoogleAuthProvider.getCredential...")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Log.d("SignIn", "getCredential successful. Calling auth.signInWithCredential...")
                val authResult = auth.signInWithCredential(credential).await()
                Log.d("SignIn", "signInWithCredential successful. User: ${authResult.user?.uid}")
                _uiState.update { it.copy(currentUser = authResult.user, signInResultMessage = "Successfully signed in!") }
            } catch (e: Exception) {
                Log.e("SignIn", "Failed to sign in with Google credential", e)
                _uiState.update { it.copy(signInResultMessage = "Failed to sign in: ${e.message}") }
            }
        }
    }

    open fun signOut(onSignedOut: () -> Unit) {
        auth.signOut()
        onSignedOut()
    }

    fun onSignInFailed(errorMessage: String) {
         _uiState.update { it.copy(signInResultMessage = errorMessage) }
    }



    fun clearSignInResultMessage() {
        _uiState.update { it.copy(signInResultMessage = null) }
    }

    private fun removeListeners() {
        userPreferencesListener?.remove()
        userPreferencesListener = null
        completedSetsListener?.remove()
        completedSetsListener = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
        Log.d("DashboardViewModel", "ViewModel cleared and listeners removed.")
    }
}
