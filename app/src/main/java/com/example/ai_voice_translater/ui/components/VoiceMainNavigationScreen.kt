package com.example.ai_voice_translater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


sealed class BottomBarItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomBarItem("home", "Home", Icons.Default.Home)
    object History : BottomBarItem("history", "History", Icons.Default.Refresh)
    object Profile : BottomBarItem("profile", "Profile", Icons.Default.Person)
}

val bottomBarItems = listOf(
    BottomBarItem.Home,
    BottomBarItem.History,
    BottomBarItem.Profile
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomAppBarWithNavigation(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomBarItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomBarItem.Home.route) { TranslatorScreen(navController) }
            composable(BottomBarItem.History.route) { UserHistory(modifier = Modifier) }
            composable(BottomBarItem.Profile.route) { ProfileSettings(modifier = Modifier) }
        }
    }
}


@Composable
fun BottomAppBarWithNavigation(navController: NavHostController) {
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route

    BottomAppBar(modifier = Modifier.background(color = Color.White)) {
        bottomBarItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    Surface (modifier = Modifier.fillMaxSize()){
        MainScreen()
    }
}
