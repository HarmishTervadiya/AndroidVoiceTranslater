// EditProfileScreen.kt
package com.example.ai_voice_translater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ai_voice_translater.data.AuthState
import com.example.ai_voice_translater.data.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // State for the text fields, initialized with user data
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Update local state when authState changes
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val userData = (authState as AuthState.Authenticated).userData
            name = userData.username ?: ""
            // Email is not part of UserData, so we'll keep it as a placeholder
            email = "Email cannot be changed"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header with back button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name field
        Text("Name", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF1F3F5),
                focusedContainerColor = Color(0xFFF1F3F5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email field (disabled)
        Text("Email", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = email,
            onValueChange = { /* No-op */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = false, // Disable the email field
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF1F3F5)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                coroutineScope.launch {
                    authViewModel.updateUserProfile(name)
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text("Save", color = Color.White)
        }
    }
}
