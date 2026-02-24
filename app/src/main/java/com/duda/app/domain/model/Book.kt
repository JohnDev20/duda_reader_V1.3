package com.duda.app.domain.model

import com.duda.app.data.local.entity.BookCategory
import com.duda.app.data.local.entity.BookFormat

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String,
    val filePath: String,
    val coverPath: String? = null,
    val format: BookFormat,
    val category: BookCategory = BookCategory.NEW,
    val lastPageRead: Int = 0,
    val readingProgressPercent: Float = 0f,
    val scrollPositionY: Int = 0,
    val totalPages: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
