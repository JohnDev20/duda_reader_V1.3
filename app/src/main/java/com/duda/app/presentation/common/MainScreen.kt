package com.duda.app.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.duda.app.core.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun MainScreen(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Library,
            label = "Biblioteca",
            icon = { Icon(Icons.Filled.Book, contentDescription = "Biblioteca") }
        ),
        BottomNavItem(
            screen = Screen.Vocabulary,
            label = "Vocabulário",
            icon = { Icon(Icons.Filled.Translate, contentDescription = "Vocabulário") }
        ),
        BottomNavItem(
            screen = Screen.Categories,
            label = "Categorias",
            icon = { Icon(Icons.Filled.Category, contentDescription = "Categorias") }
        ),
        BottomNavItem(
            screen = Screen.Settings,
            label = "Config",
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") }
        )
    )

    val bottomNavRoutes = bottomNavItems.map { it.screen.route }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Só mostra a Bottom Bar nas telas raiz
    val showBottomBar = bottomNavRoutes.any { route ->
        currentRoute == route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = item.icon,
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
