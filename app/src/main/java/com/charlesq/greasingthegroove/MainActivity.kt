package com.charlesq.greasingthegroove

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.charlesq.greasingthegroove.ui.screens.DashboardScreen
import com.charlesq.greasingthegroove.ui.screens.ExerciseConfigurationScreen
import com.charlesq.greasingthegroove.ui.screens.SettingsScreen
import com.charlesq.greasingthegroove.ui.screens.WelcomeScreen
import com.charlesq.greasingthegroove.ui.theme.GreasingTheGrooveTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialManager = CredentialManager.create(this)
        setContent {
            val theme by settingsViewModel.theme.collectAsState()
            val useDarkTheme = when (theme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            GreasingTheGrooveTheme(darkTheme = useDarkTheme) {
                val coroutineScope = rememberCoroutineScope()
                val uiState by dashboardViewModel.uiState.collectAsState()

                GreasingTheGrooveApp(
                    uiState = uiState,
                    onSignInClick = {
                        coroutineScope.launch {
                            startGoogleSignIn()
                        }
                    },
                    onSignOutClick = {
                        dashboardViewModel.signOut {
                            coroutineScope.launch {
                                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            }
                        }
                    },
                    onClearSignInResultMessage = { dashboardViewModel.clearSignInResultMessage() }
                )
            }
        }
    }

    private suspend fun startGoogleSignIn() {
        Log.d("SignIn", "startGoogleSignIn called")
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()
        Log.d("SignIn", "GetGoogleIdOption built")

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        Log.d("SignIn", "GetCredentialRequest built")

        try {
            Log.d("SignIn", "Calling credentialManager.getCredential...")
            val result = credentialManager.getCredential(this, request)
            Log.d("SignIn", "getCredential successful")
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    Log.d("SignIn", "Successfully created GoogleIdTokenCredential. Calling viewModel to sign in.")
                    dashboardViewModel.signInWithGoogleCredential(googleIdTokenCredential.idToken)
                } catch (e: Exception) {
                    Log.e("SignIn", "Exception while creating GoogleIdTokenCredential", e)
                }
            } else {
                Log.e("SignIn", "Unexpected credential type: ${credential::class.java.name}")
            }
        } catch (e: GetCredentialException) {
            Log.e("SignIn", "Google sign in failed with GetCredentialException", e)
        } catch (e: Exception) {
            Log.e("SignIn", "An unexpected error occurred during sign-in", e)
        }
    }
}

@Composable
fun GreasingTheGrooveApp(
    uiState: DashboardUiState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onClearSignInResultMessage: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isAuthenticating) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.currentUser == null) {
            WelcomeScreen(onSignInClick = onSignInClick)
        } else {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToExerciseConfiguration = { navController.navigate("exercise_configuration") }
                    )
                }
                composable("settings") {
                    val dashboardViewModel: DashboardViewModel = viewModel()
                    SettingsScreen(
                        dashboardViewModel = dashboardViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onSignOut = onSignOutClick
                    )
                }
                composable("exercise_configuration") { navBackStackEntry ->
                    ExerciseConfigurationScreen(
                        navBackStackEntry = navBackStackEntry,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToExercisePicker = { slotIndex ->
                            navController.navigate("exercise_picker/$slotIndex")
                        }
                    )
                }
                composable(
                    "exercise_picker/{slotIndex}",
                    arguments = listOf(navArgument("slotIndex") { type = NavType.IntType })
                ) { backStackEntry ->
                    val slotIndex = backStackEntry.arguments?.getInt("slotIndex") ?: -1
                    ExercisePickerScreen(
                        navController = navController,
                        slotIndex = slotIndex
                    )
                }
            }
        }
    }

    uiState.signInResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onClearSignInResultMessage,
            title = { Text("Sign-In Result") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onClearSignInResultMessage) {
                    Text("OK")
                }
            }
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
