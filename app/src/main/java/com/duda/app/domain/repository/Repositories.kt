package com.duda.app.domain.repository

import com.duda.app.data.local.entity.BookCategory
import com.duda.app.domain.model.Book
import com.duda.app.domain.model.Highlight
import com.duda.app.domain.model.Tag
import com.duda.app.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getBooksByCategory(category: BookCategory): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun insertBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(book: Book)
    fun searchBooks(query: String): Flow<List<Book>>
    suspend fun updateReadingProgress(
        bookId: Long,
        lastPageRead: Int,
        progressPercent: Float,
        scrollPositionY: Int
    )
    suspend fun updateCategory(bookId: Long, category: BookCategory)
}

interface HighlightRepository {
    fun getHighlightsByBook(bookId: Long): Flow<List<Highlight>>
    suspend fun insertHighlight(highlight: Highlight): Long
    suspend fun deleteHighlight(highlight: Highlight)
}

interface VocabularyRepository {
    fun getAllVocabulary(): Flow<List<Vocabulary>>
    suspend fun insertVocabulary(vocabulary: Vocabulary): Long
    suspend fun deleteVocabulary(vocabulary: Vocabulary)
    fun searchVocabulary(query: String): Flow<List<Vocabulary>>
}

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun insertTag(tag: Tag): Long
    suspend fun deleteTag(tag: Tag)
    fun getTagsForBook(bookId: Long): Flow<List<Tag>>
    suspend fun addTagToBook(bookId: Long, tagId: Long)
    suspend fun removeTagFromBook(bookId: Long, tagId: Long)
}

interface DictionaryRepository {
    suspend fun getDefinition(word: String): Result<String>
}
