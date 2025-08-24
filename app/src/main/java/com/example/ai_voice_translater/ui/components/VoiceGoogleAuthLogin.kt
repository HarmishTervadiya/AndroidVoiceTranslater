// AppNavigation.kt
package com.example.ai_voice_translater.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ai_voice_translater.data.AuthState
import com.example.ai_voice_translater.data.AuthViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // The ViewModel is hoisted here to be used by the whole navigation graph
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // This effect will react to changes in authState and navigate automatically
    LaunchedEffect(key1 = authState) {
        if (authState is AuthState.Authenticated) {
            // If logged in, go to the main app flow
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (authState is AuthState.Unauthenticated) {
            // THIS IS THE CODE THAT REDIRECTS ON LOGOUT
            navController.navigate(Screen.Login.route) {
                // ================== THE FIX ==================
                // This is a more robust way to clear the back stack.
                // It pops all destinations until the very start of the graph.
                // =============================================
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                // Ensure we don't create multiple copies of the login screen
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            // Pass the same ViewModel instance down to the login screen
            UserLoginScreen(authViewModel = authViewModel)
        }
        composable(Screen.Main.route) {
            // MainScreen will also use this ViewModel for sign-out
            MainScreen(navController)
        }
    }
}


@Composable
fun UserLoginScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()

    // This launcher handles the result from the Google Sign-In activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // The ViewModel now processes the result directly
                result.data?.let { intent ->
                    authViewModel.processSignInResult(intent)
                }
            }
        }
    )

    Column(modifier = Modifier
        .systemBarsPadding()
        .fillMaxWidth()
        .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Translate",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Welcome to Translate",
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Translate text, voice, and images in over 100 languages.",
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Normal,
            fontSize = 15.sp,
            modifier = Modifier.width(300.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Show a loading spinner while authenticating
        if (authState is AuthState.Authenticated) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Get the sign-in intent directly from the ViewModel
                        val signInIntentSender = authViewModel.getSignInIntent()
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                signInIntentSender ?: return@launch
                            ).build()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D78F2)
                )
            ) {
                Text(text = "Sign in with Google",
                    fontWeight = FontWeight.SemiBold)
            }
        }

        // Display any error messages from the ViewModel
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserLoginScreenPreview() {
    // Preview the login screen directly
    UserLoginScreen()
}
