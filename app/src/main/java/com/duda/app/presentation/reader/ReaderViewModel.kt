package com.duda.app.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duda.app.domain.model.Book
import com.duda.app.domain.model.Highlight
import com.duda.app.domain.model.Vocabulary
import com.duda.app.domain.usecase.GetBookByIdUseCase
import com.duda.app.domain.usecase.GetHighlightsUseCase
import com.duda.app.domain.usecase.SaveHighlightUseCase
import com.duda.app.domain.usecase.SearchWordDefinitionUseCase
import com.duda.app.domain.usecase.UpdateReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val book: Book? = null,
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedText: String = "",
    val wordDefinition: Vocabulary? = null,
    val isSearchingWord: Boolean = false,
    val wordSearchError: String? = null,
    val showHighlightDialog: Boolean = false,
    val fontSize: Float = 16f
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getHighlightsUseCase: GetHighlightsUseCase,
    private val saveHighlightUseCase: SaveHighlightUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val searchWordDefinitionUseCase: SearchWordDefinitionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val book = getBookByIdUseCase(bookId)
            if (book != null) {
                _uiState.value = _uiState.value.copy(
                    book = book,
                    currentPage = book.lastPageRead,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Livro não encontrado."
                )
            }
        }
    }

    fun onPageChanged(page: Int, totalPages: Int, scrollY: Int = 0) {
        val book = _uiState.value.book ?: return
        val progress = if (totalPages > 0) page.toFloat() / totalPages else 0f
        _uiState.value = _uiState.value.copy(currentPage = page)

        viewModelScope.launch {
            updateReadingProgressUseCase(
                bookId = book.id,
                lastPageRead = page,
                progressPercent = progress,
                scrollPositionY = scrollY
            )
        }
    }

    fun onTextSelected(text: String) {
        _uiState.value = _uiState.value.copy(
            selectedText = text,
            wordDefinition = null,
            wordSearchError = null
        )
    }

    fun searchWordDefinition() {
        val word = _uiState.value.selectedText.trim()
        if (word.isBlank()) return

        val book = _uiState.value.book
        _uiState.value = _uiState.value.copy(
            isSearchingWord = true,
            wordSearchError = null
        )

        viewModelScope.launch {
            val result = searchWordDefinitionUseCase(
                word = word,
                bookId = book?.id,
                pageNumber = _uiState.value.currentPage
            )
            result.fold(
                onSuccess = { vocabulary ->
                    _uiState.value = _uiState.value.copy(
                        wordDefinition = vocabulary,
                        isSearchingWord = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        wordSearchError = error.message,
                        isSearchingWord = false
                    )
                }
            )
        }
    }

    fun saveHighlight(text: String, color: Int) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            saveHighlightUseCase(
                Highlight(
                    bookId = book.id,
                    text = text,
                    color = color,
                    pageNumber = _uiState.value.currentPage
                )
            )
            _uiState.value = _uiState.value.copy(showHighlightDialog = false)
        }
    }

    fun showHighlightDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showHighlightDialog = show)
    }

    fun dismissWordDefinition() {
        _uiState.value = _uiState.value.copy(
            wordDefinition = null,
            wordSearchError = null,
            selectedText = ""
        )
    }

    fun setFontSize(size: Float) {
        _uiState.value = _uiState.value.copy(fontSize = size)
    }
}
