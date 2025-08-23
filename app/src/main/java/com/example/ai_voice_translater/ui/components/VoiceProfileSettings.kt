// ProfileScreen.kt
package com.example.ai_voice_translater.ui.components

import android.text.Layout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ai_voice_translater.data.AuthState
import com.example.ai_voice_translater.data.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // Color theme matching TranslatorScreen
    val primaryBlue = Color(0xFF007BFF)
    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0056B3)

    Scaffold(
        containerColor = Color.White,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = authState is AuthState.Authenticated,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (authState is AuthState.Authenticated) {
                    val userData = (authState as AuthState.Authenticated).userData

                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Profile Header Card
                        ProfileHeaderCard(
                            userData = userData,
                            primaryBlue = primaryBlue,
                            lightBlue = lightBlue
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Settings Section
                        SettingsSection(
                            onEditProfile = { navController.navigate("edit_profile") },
                            onLogout = { authViewModel.signOut() },
                            primaryBlue = primaryBlue,
                            lightBlue = lightBlue,
                            darkBlue = darkBlue
                        )
                    }
                }
            }

            // Loading state
            if (authState !is AuthState.Authenticated) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = primaryBlue,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading profile...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeaderCard(
    userData: com.example.ai_voice_translater.data.UserData,
    primaryBlue: Color,
    lightBlue: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 15.dp, horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Profile Image with gradient border
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Gradient border background
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryBlue, lightBlue, primaryBlue)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner white circle for padding
                    Box(
//                        modifier = Modifier
//                            .size(112.dp)
//                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userData.profilePictureUrl?.isNotBlank() == true) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = userData.profilePictureUrl
                                ),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Default avatar icon
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(lightBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Avatar",
                                    tint = primaryBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Name
            Text(
                text = userData.username ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = primaryBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User Email
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = lightBlue.copy(alpha = 0.3f)
            ) {
                Text(
                    text = userData.email ?: "user@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryBlue,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    primaryBlue: Color,
    lightBlue: Color,
    darkBlue: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Settings Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(lightBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = primaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryBlue
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Edit Profile Setting
            ModernSettingItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                subtitle = "Update your personal information",
                onClick = onEditProfile,
                primaryBlue = primaryBlue,
                lightBlue = lightBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Logout Setting (with different styling)
            ModernSettingItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Logout",
                subtitle = "Sign out of your account",
                onClick = onLogout,
                primaryBlue = Color(0xFFFF4444),
                lightBlue = Color(0xFFFFEBEE),
                isDestructive = true
            )
        }
    }
}

@Composable
fun ModernSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    primaryBlue: Color,
    lightBlue: Color,
    isDestructive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = lightBlue.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isDestructive) primaryBlue.copy(alpha = 0.1f) else lightBlue,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = primaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDestructive) primaryBlue else primaryBlue
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDestructive) primaryBlue.copy(alpha = 0.7f) else Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = primaryBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        // Preview implementation would go here
    }
}