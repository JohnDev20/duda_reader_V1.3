package com.duda.app.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duda.app.data.local.entity.BookCategory
import com.duda.app.domain.model.Book
import com.duda.app.domain.usecase.GetBooksUseCase
import com.duda.app.domain.usecase.SearchBooksUseCase
import com.duda.app.domain.usecase.UpdateBookCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryViewMode { GRID, CAROUSEL }

data class LibraryUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val viewMode: LibraryViewMode = LibraryViewMode.GRID,
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val updateBookCategoryUseCase: UpdateBookCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val books: StateFlow<List<Book>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) getBooksUseCase()
            else searchBooksUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleViewMode() {
        val newMode = if (_uiState.value.viewMode == LibraryViewMode.GRID) {
            LibraryViewMode.CAROUSEL
        } else {
            LibraryViewMode.GRID
        }
        _uiState.value = _uiState.value.copy(viewMode = newMode)
    }

    fun updateCategory(bookId: Long, category: BookCategory) {
        viewModelScope.launch {
            updateBookCategoryUseCase(bookId, category)
        }
    }
}
