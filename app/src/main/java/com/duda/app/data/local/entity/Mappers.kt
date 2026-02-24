package com.duda.app.data.local.entity

import com.duda.app.domain.model.Book
import com.duda.app.domain.model.Highlight
import com.duda.app.domain.model.Tag
import com.duda.app.domain.model.Vocabulary

// ─── Book ─────────────────────────────────────────────────────────────────────

fun BookEntity.toDomain(): Book = Book(
    id = id,
    title = title,
    author = author,
    filePath = filePath,
    coverPath = coverPath,
    format = format,
    category = category,
    lastPageRead = lastPageRead,
    readingProgressPercent = readingProgressPercent,
    scrollPositionY = scrollPositionY,
    totalPages = totalPages,
    addedAt = addedAt,
    updatedAt = updatedAt
)

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    filePath = filePath,
    coverPath = coverPath,
    format = format,
    category = category,
    lastPageRead = lastPageRead,
    readingProgressPercent = readingProgressPercent,
    scrollPositionY = scrollPositionY,
    totalPages = totalPages,
    addedAt = addedAt,
    updatedAt = updatedAt
)

// ─── Highlight ────────────────────────────────────────────────────────────────

fun HighlightEntity.toDomain(): Highlight = Highlight(
    id = id,
    bookId = bookId,
    text = text,
    note = note,
    color = color,
    pageNumber = pageNumber,
    createdAt = createdAt
)

fun Highlight.toEntity(): HighlightEntity = HighlightEntity(
    id = id,
    bookId = bookId,
    text = text,
    note = note,
    color = color,
    pageNumber = pageNumber,
    createdAt = createdAt
)

// ─── Vocabulary ───────────────────────────────────────────────────────────────

fun VocabularyEntity.toDomain(): Vocabulary = Vocabulary(
    id = id,
    word = word,
    definition = definition,
    bookId = bookId,
    pageNumber = pageNumber,
    createdAt = createdAt
)

fun Vocabulary.toEntity(): VocabularyEntity = VocabularyEntity(
    id = id,
    word = word,
    definition = definition,
    bookId = bookId,
    pageNumber = pageNumber,
    createdAt = createdAt
)

// ─── Tag ──────────────────────────────────────────────────────────────────────

fun TagEntity.toDomain(): Tag = Tag(id = id, name = name, color = color)
fun Tag.toEntity(): TagEntity = TagEntity(id = id, name = name, color = color)
