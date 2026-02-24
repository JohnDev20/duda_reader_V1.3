package com.duda.app.data.repository

import com.duda.app.data.local.dao.BookDao
import com.duda.app.data.local.dao.HighlightDao
import com.duda.app.data.local.dao.TagDao
import com.duda.app.data.local.dao.VocabularyDao
import com.duda.app.data.local.entity.BookCategory
import com.duda.app.data.local.entity.BookTagCrossRef
import com.duda.app.data.local.entity.toDomain
import com.duda.app.data.local.entity.toEntity
import com.duda.app.data.remote.api.DictionaryApiService
import com.duda.app.data.remote.dto.toFormattedDefinition
import com.duda.app.domain.model.Book
import com.duda.app.domain.model.Highlight
import com.duda.app.domain.model.Tag
import com.duda.app.domain.model.Vocabulary
import com.duda.app.domain.repository.BookRepository
import com.duda.app.domain.repository.DictionaryRepository
import com.duda.app.domain.repository.HighlightRepository
import com.duda.app.domain.repository.TagRepository
import com.duda.app.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ─── BookRepositoryImpl ───────────────────────────────────────────────────────

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> =
        bookDao.getAllBooks().map { entities -> entities.map { it.toDomain() } }

    override fun getBooksByCategory(category: BookCategory): Flow<List<Book>> =
        bookDao.getBooksByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getBookById(id: Long): Book? =
        bookDao.getBookById(id)?.toDomain()

    override suspend fun insertBook(book: Book): Long =
        bookDao.insertBook(book.toEntity())

    override suspend fun updateBook(book: Book) =
        bookDao.updateBook(book.toEntity())

    override suspend fun deleteBook(book: Book) =
        bookDao.deleteBook(book.toEntity())

    override fun searchBooks(query: String): Flow<List<Book>> =
        bookDao.searchBooks(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun updateReadingProgress(
        bookId: Long,
        lastPageRead: Int,
        progressPercent: Float,
        scrollPositionY: Int
    ) = bookDao.updateReadingProgress(bookId, lastPageRead, progressPercent, scrollPositionY)

    override suspend fun updateCategory(bookId: Long, category: BookCategory) =
        bookDao.updateCategory(bookId, category)
}

// ─── HighlightRepositoryImpl ──────────────────────────────────────────────────

class HighlightRepositoryImpl @Inject constructor(
    private val highlightDao: HighlightDao
) : HighlightRepository {

    override fun getHighlightsByBook(bookId: Long): Flow<List<Highlight>> =
        highlightDao.getHighlightsByBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertHighlight(highlight: Highlight): Long =
        highlightDao.insertHighlight(highlight.toEntity())

    override suspend fun deleteHighlight(highlight: Highlight) =
        highlightDao.deleteHighlight(highlight.toEntity())
}

// ─── VocabularyRepositoryImpl ─────────────────────────────────────────────────

class VocabularyRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao
) : VocabularyRepository {

    override fun getAllVocabulary(): Flow<List<Vocabulary>> =
        vocabularyDao.getAllVocabulary().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertVocabulary(vocabulary: Vocabulary): Long =
        vocabularyDao.insertVocabulary(vocabulary.toEntity())

    override suspend fun deleteVocabulary(vocabulary: Vocabulary) =
        vocabularyDao.deleteVocabulary(vocabulary.toEntity())

    override fun searchVocabulary(query: String): Flow<List<Vocabulary>> =
        vocabularyDao.searchVocabulary(query).map { entities -> entities.map { it.toDomain() } }
}

// ─── TagRepositoryImpl ────────────────────────────────────────────────────────

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> =
        tagDao.getAllTags().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertTag(tag: Tag): Long =
        tagDao.insertTag(tag.toEntity())

    override suspend fun deleteTag(tag: Tag) =
        tagDao.deleteTag(tag.toEntity())

    override fun getTagsForBook(bookId: Long): Flow<List<Tag>> =
        tagDao.getTagsForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTagToBook(bookId: Long, tagId: Long) =
        tagDao.addTagToBook(BookTagCrossRef(bookId, tagId))

    override suspend fun removeTagFromBook(bookId: Long, tagId: Long) =
        tagDao.removeTagFromBook(bookId, tagId)
}

// ─── DictionaryRepositoryImpl ─────────────────────────────────────────────────

class DictionaryRepositoryImpl @Inject constructor(
    private val apiService: DictionaryApiService
) : DictionaryRepository {

    override suspend fun getDefinition(word: String): Result<String> {
        return try {
            val response = apiService.getDefinition(word.trim().lowercase())
            Result.success(response.toFormattedDefinition())
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                Result.failure(Exception("Palavra \"$word\" não encontrada no dicionário."))
            } else {
                Result.failure(Exception("Erro ao buscar definição: ${e.message}"))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Sem conexão com a internet."))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.message}"))
        }
    }
}
