package com.duda.app.domain.model

data class Highlight(
    val id: Long = 0,
    val bookId: Long,
    val text: String,
    val note: String = "",
    val color: Int = 0xFFFFD700.toInt(), // Amarelo padrão
    val pageNumber: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Vocabulary(
    val id: Long = 0,
    val word: String,
    val definition: String,
    val bookId: Long? = null,
    val pageNumber: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Int = 0xFF6200EE.toInt()
)
