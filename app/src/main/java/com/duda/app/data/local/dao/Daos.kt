package com.duda.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.duda.app.data.local.entity.BookCategory
import com.duda.app.data.local.entity.BookEntity
import com.duda.app.data.local.entity.BookTagCrossRef
import com.duda.app.data.local.entity.HighlightEntity
import com.duda.app.data.local.entity.TagEntity
import com.duda.app.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

// ─── BookDao ──────────────────────────────────────────────────────────────────

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY updated_at DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE category = :category ORDER BY updated_at DESC")
    fun getBooksByCategory(category: BookCategory): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("""
        SELECT * FROM books 
        WHERE title LIKE '%' || :query || '%' 
        OR author LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
    """)
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("""
        UPDATE books 
        SET last_page_read = :lastPageRead,
            reading_progress_percent = :progressPercent,
            scroll_position_y = :scrollPositionY,
            updated_at = :updatedAt
        WHERE id = :bookId
    """)
    suspend fun updateReadingProgress(
        bookId: Long,
        lastPageRead: Int,
        progressPercent: Float,
        scrollPositionY: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE books SET category = :category, updated_at = :updatedAt WHERE id = :bookId")
    suspend fun updateCategory(
        bookId: Long,
        category: BookCategory,
        updatedAt: Long = System.currentTimeMillis()
    )
}

// ─── HighlightDao ─────────────────────────────────────────────────────────────

@Dao
interface HighlightDao {

    @Query("SELECT * FROM highlights WHERE book_id = :bookId ORDER BY page_number ASC")
    fun getHighlightsByBook(bookId: Long): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity): Long

    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)
}

// ─── VocabularyDao ────────────────────────────────────────────────────────────

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabulary ORDER BY created_at DESC")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long

    @Delete
    suspend fun deleteVocabulary(vocabulary: VocabularyEntity)

    @Query("""
        SELECT * FROM vocabulary 
        WHERE word LIKE '%' || :query || '%' 
        OR definition LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun searchVocabulary(query: String): Flow<List<VocabularyEntity>>
}

// ─── TagDao ───────────────────────────────────────────────────────────────────

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN book_tags bt ON t.id = bt.tag_id
        WHERE bt.book_id = :bookId
    """)
    fun getTagsForBook(bookId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToBook(crossRef: BookTagCrossRef)

    @Query("DELETE FROM book_tags WHERE book_id = :bookId AND tag_id = :tagId")
    suspend fun removeTagFromBook(bookId: Long, tagId: Long)
}
