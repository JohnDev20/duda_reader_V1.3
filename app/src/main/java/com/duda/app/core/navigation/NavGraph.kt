package com.duda.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.duda.app.presentation.bookdetail.BookDetailScreen
import com.duda.app.presentation.importbook.ImportScreen
import com.duda.app.presentation.library.LibraryScreen
import com.duda.app.presentation.reader.ReaderScreen
import com.duda.app.presentation.settings.SettingsScreen
import com.duda.app.presentation.vocabulary.VocabularyScreen
import com.duda.app.presentation.library.CategoriesScreen

@Composable
fun DudaNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Library.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Library.route) {
            LibraryScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.Reader.createRoute(bookId))
                },
                onBookDetailClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onImportClick = {
                    navController.navigate(Screen.Import.route)
                }
            )
        }

        composable(Screen.Vocabulary.route) {
            VocabularyScreen()
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.Reader.createRoute(bookId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            BookDetailScreen(
                bookId = bookId,
                onReadClick = { navController.navigate(Screen.Reader.createRoute(bookId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Reader.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            ReaderScreen(
                bookId = bookId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Import.route) {
            ImportScreen(
                onImportComplete = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
