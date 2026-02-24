package com.duda.app.core.navigation

sealed class Screen(val route: String) {
    // Bottom nav roots
    object Library : Screen("library")
    object Vocabulary : Screen("vocabulary")
    object Categories : Screen("categories")
    object Settings : Screen("settings")

    // Nested screens
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }

    object Reader : Screen("reader/{bookId}") {
        fun createRoute(bookId: Long) = "reader/$bookId"
    }

    object Import : Screen("import")
}
