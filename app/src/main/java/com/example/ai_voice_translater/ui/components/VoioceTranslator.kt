// TranslatorScreen.kt
package com.example.ai_voice_translater.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ai_voice_translater.data.MainViewModel
import com.example.ai_voice_translater.data.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val selectedLanguageCode by mainViewModel.selectedLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    // ▼▼▼▼▼ PERMISSION HANDLING START ▼▼▼▼▼

    val context = LocalContext.current

    // 1. Create a permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, start recording
                mainViewModel.toggleRecording()
            } else {
                // Permission denied, show a toast
                Toast.makeText(context, "Permission denied. Cannot record audio.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // ▲▲▲▲▲ PERMISSION HANDLING END ▲▲▲▲▲


    // Language map: Display Name to API Code
    val languages = mapOf(
        "English" to "en-IN",
        "Hindi" to "hi-IN",
        "Gujarati" to "gu-IN",
        "Spanish" to "es-ES"
    )
    val selectedLanguageName = languages.entries.find { it.value == selectedLanguageCode }?.key ?: "Select Language"

    // Reset the UI state when the screen is first composed or recomposed
    LaunchedEffect(Unit) {
        mainViewModel.resetState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Mic Icon Button
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = if (uiState is UiState.Recording) Color.Red else Color(0xFF007BFF),
                        shape = CircleShape
                    )
                    .clickable {
                        // ▼▼▼▼▼ PERMISSION HANDLING START ▼▼▼▼▼

                        // If already recording, stop it. No permission needed for this.
                        if (uiState is UiState.Recording) {
                            mainViewModel.toggleRecording()
                        } else {
                            // 2. Check for permission before starting
                            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                mainViewModel.toggleRecording()
                            } else {
                                // 3. Request permission if not granted
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }

                        // ▲▲▲▲▲ PERMISSION HANDLING END ▲▲▲▲▲
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (uiState) {
                    is UiState.Recording -> "Recording..."
                    is UiState.Loading -> "Translating..."
                    else -> "Start Recording"
                },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Language Dropdown
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedLanguageName)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { (name, code) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                mainViewModel.setLanguage(code)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result Text Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Allow box to grow
                    .background(Color(0xFFE6F0FF), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF007BFF), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                val displayText = when (val state = uiState) {
                    is UiState.Success -> state.translation
                    is UiState.Error -> "Error: ${state.message}"
                    else -> ""
                }
                Text(text = displayText, color = if (uiState is UiState.Error) Color.Red else Color(0xFF007BFF))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}