// MainScreen.kt
package com.example.ai_voice_translater.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Define the items for the bottom bar with modern styling
sealed class BottomBarScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomBarScreen("home", "Translate", Icons.Default.Translate)
    object History : BottomBarScreen("history", "History", Icons.Default.History)
    object Profile : BottomBarScreen("profile", "Profile", Icons.Default.Person)
}

private val bottomBarItems = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.History,
    BottomBarScreen.Profile,
)

// This is the main screen for authenticated users, containing the bottom navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavHostController) {
    val nestedNavController = rememberNavController()

    // Color theme matching the translator screen
    val primaryBlue = Color(0xFF007BFF)
    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0056B3)

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            ModernBottomNavBar(
                navController = nestedNavController,
                primaryBlue = primaryBlue,
                lightBlue = lightBlue,
                darkBlue = darkBlue
            )
        },

    ) { innerPadding ->
        // This NavHost handles navigation between the bottom bar items AND to other screens
        NavHost(
            navController = nestedNavController,
            startDestination = BottomBarScreen.Home.route,
//            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomBarScreen.Home.route) {
                TranslatorScreen(nestedNavController)
            }
            composable(BottomBarScreen.History.route) {
                UserHistoryScreen(nestedNavController)
            }
            composable(BottomBarScreen.Profile.route) {
                ProfileScreen(nestedNavController)
            }
            // The EditProfile screen is a destination within this graph
            composable("edit_profile") {
                EditProfileScreen(nestedNavController)
            }
        }
    }
}

@Composable
fun ModernBottomNavBar(
    navController: NavHostController,
    primaryBlue: Color,
    lightBlue: Color,
    darkBlue: Color
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Modern elevated navigation bar with custom styling
    Surface(
        modifier = Modifier
//            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
//        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            lightBlue.copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
        ) {
            bottomBarItems.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    icon = {
                        ModernNavIcon(
                            icon = screen.icon,
                            label = screen.label,
                            isSelected = isSelected,
                            primaryBlue = primaryBlue,
                            lightBlue = lightBlue
                        )
                    },
                    label = {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                            exit = fadeOut(tween(150)) + scaleOut(tween(150))
                        ) {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryBlue
                            )
                        }

                        // Always show label but make it transparent when not selected
                        if (!isSelected) {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryBlue,
                        selectedTextColor = primaryBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent // We'll use custom indicator
                    )
                )
            }
        }
    }
}

@Composable
fun ModernNavIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    primaryBlue: Color,
    lightBlue: Color
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Custom indicator background
        if (isSelected) {
            Box(
                modifier = Modifier
//                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                lightBlue.copy(alpha = 0.3f),
                                lightBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
//                            radius = 80f
                        ),
//                        shape = CircleShape
                    )
            )

            // Inner selected circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryBlue.copy(alpha = 0.15f), lightBlue.copy(alpha = 0.25f))
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Icon with animation
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(tween(200)),
            exit = scaleOut(tween(150))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) primaryBlue else Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.size(if (isSelected) 26.dp else 24.dp)
            )
        }
    }
}

// Extension function for better theme integration
@Composable
fun NavigationBarDefaults.modernColors(
    primaryBlue: Color,
    lightBlue: Color
) = NavigationBarItemDefaults.colors(
    selectedIconColor = primaryBlue,
    selectedTextColor = primaryBlue,
    unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
    unselectedTextColor = Color.Gray.copy(alpha = 0.8f),
    indicatorColor = lightBlue.copy(alpha = 0.2f)
)