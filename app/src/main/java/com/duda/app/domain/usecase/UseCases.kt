package com.duda.app.domain.usecase

import com.duda.app.data.local.entity.BookCategory
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
import javax.inject.Inject

// ─── Book UseCases ────────────────────────────────────────────────────────────

class GetBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> = repository.getAllBooks()
}

class GetBooksByCategoryUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(category: BookCategory): Flow<List<Book>> =
        repository.getBooksByCategory(category)
}

class GetBookByIdUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(id: Long): Book? = repository.getBookById(id)
}

class ImportBookUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(book: Book): Long = repository.insertBook(book)
}

class DeleteBookUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(book: Book) = repository.deleteBook(book)
}

class SearchBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(query: String): Flow<List<Book>> = repository.searchBooks(query)
}

class UpdateReadingProgressUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(
        bookId: Long,
        lastPageRead: Int,
        progressPercent: Float,
        scrollPositionY: Int
    ) = repository.updateReadingProgress(bookId, lastPageRead, progressPercent, scrollPositionY)
}

class UpdateBookCategoryUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: Long, category: BookCategory) =
        repository.updateCategory(bookId, category)
}

// ─── Highlight UseCases ───────────────────────────────────────────────────────

class GetHighlightsUseCase @Inject constructor(
    private val repository: HighlightRepository
) {
    operator fun invoke(bookId: Long): Flow<List<Highlight>> =
        repository.getHighlightsByBook(bookId)
}

class SaveHighlightUseCase @Inject constructor(
    private val repository: HighlightRepository
) {
    suspend operator fun invoke(highlight: Highlight): Long =
        repository.insertHighlight(highlight)
}

class DeleteHighlightUseCase @Inject constructor(
    private val repository: HighlightRepository
) {
    suspend operator fun invoke(highlight: Highlight) =
        repository.deleteHighlight(highlight)
}

// ─── Vocabulary UseCases ──────────────────────────────────────────────────────

class GetVocabularyUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    operator fun invoke(): Flow<List<Vocabulary>> = repository.getAllVocabulary()
}

class SearchVocabularyUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    operator fun invoke(query: String): Flow<List<Vocabulary>> =
        repository.searchVocabulary(query)
}

class SaveVocabularyUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(vocabulary: Vocabulary): Long =
        repository.insertVocabulary(vocabulary)
}

class DeleteVocabularyUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(vocabulary: Vocabulary) =
        repository.deleteVocabulary(vocabulary)
}

// ─── Dictionary UseCase ───────────────────────────────────────────────────────

class SearchWordDefinitionUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val vocabularyRepository: VocabularyRepository
) {
    /**
     * Busca a definição online e salva no vocabulário local.
     * Retorna Result com a definição ou erro (ex: offline, palavra não encontrada).
     */
    suspend operator fun invoke(
        word: String,
        bookId: Long? = null,
        pageNumber: Int = 0
    ): Result<Vocabulary> {
        val definitionResult = dictionaryRepository.getDefinition(word)
        return definitionResult.map { definition ->
            val vocabulary = Vocabulary(
                word = word,
                definition = definition,
                bookId = bookId,
                pageNumber = pageNumber
            )
            val id = vocabularyRepository.insertVocabulary(vocabulary)
            vocabulary.copy(id = id)
        }
    }
}

// ─── Tag UseCases ─────────────────────────────────────────────────────────────

class GetTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    operator fun invoke(): Flow<List<Tag>> = repository.getAllTags()
}

class GetTagsForBookUseCase @Inject constructor(
    private val repository: TagRepository
) {
    operator fun invoke(bookId: Long): Flow<List<Tag>> = repository.getTagsForBook(bookId)
}

class SaveTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tag: Tag): Long = repository.insertTag(tag)
}

class AddTagToBookUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(bookId: Long, tagId: Long) =
        repository.addTagToBook(bookId, tagId)
}

class RemoveTagFromBookUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(bookId: Long, tagId: Long) =
        repository.removeTagFromBook(bookId, tagId)
}
