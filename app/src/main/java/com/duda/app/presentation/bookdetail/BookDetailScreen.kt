package com.duda.app.presentation.bookdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.duda.app.data.local.entity.BookCategory
import com.duda.app.domain.model.Book
import com.duda.app.domain.usecase.GetBookByIdUseCase
import com.duda.app.domain.usecase.UpdateBookCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookCategoryUseCase: UpdateBookCategoryUseCase
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            _book.value = getBookByIdUseCase(bookId)
        }
    }

    fun updateCategory(bookId: Long, category: BookCategory) {
        viewModelScope.launch {
            updateBookCategoryUseCase(bookId, category)
            _book.value = _book.value?.copy(category = category)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel = hiltViewModel(),
    onReadClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val book by viewModel.book.collectAsStateWithLifecycle()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        book?.let { currentBook ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Capa
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentBook.coverPath != null) {
                            AsyncImage(
                                model = currentBook.coverPath,
                                contentDescription = currentBook.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(180.dp, 260.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Book,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(currentBook.title, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        currentBook.author.ifBlank { "Autor desconhecido" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                item {
                    // Progresso
                    Text("Progresso", style = MaterialTheme.typography.titleSmall)
                    LinearProgressIndicator(
                        progress = { currentBook.readingProgressPercent },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${(currentBook.readingProgressPercent * 100).toInt()}% lido",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                item {
                    // Categoria
                    Text("Categoria", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BookCategory.values().forEach { category ->
                            FilterChip(
                                selected = currentBook.category == category,
                                onClick = { viewModel.updateCategory(currentBook.id, category) },
                                label = {
                                    Text(
                                        when (category) {
                                            BookCategory.NEW -> "Novo"
                                            BookCategory.READING -> "Lendo"
                                            BookCategory.READ -> "Lido"
                                            BookCategory.ABANDONED -> "Abandonado"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = onReadClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (currentBook.readingProgressPercent > 0) "Continuar lendo" else "Começar a ler")
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
