package com.duda.app.presentation.reader

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duda.app.data.local.entity.BookFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error ?: "Erro desconhecido",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = onBackClick) { Text("Voltar") }
                }
            }

            uiState.book != null -> {
                val book = uiState.book!!

                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = book.title,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                            }
                        },
                        actions = {
                            // Botão para buscar palavra selecionada
                            if (uiState.selectedText.isNotBlank()) {
                                IconButton(onClick = { viewModel.searchWordDefinition() }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Buscar significado")
                                }
                                IconButton(onClick = { viewModel.showHighlightDialog(true) }) {
                                    Icon(Icons.Filled.Bookmark, contentDescription = "Salvar highlight")
                                }
                            }
                        }
                    )

                    // Renderizador de acordo com o formato
                    when (book.format) {
                        BookFormat.PDF -> PdfReaderView(
                            filePath = book.filePath,
                            initialPage = uiState.currentPage,
                            onPageChanged = { page, total -> viewModel.onPageChanged(page, total) }
                        )
                        BookFormat.EPUB, BookFormat.HTML -> WebReaderView(
                            filePath = book.filePath,
                            format = book.format,
                            fontSize = uiState.fontSize,
                            onTextSelected = { viewModel.onTextSelected(it) },
                            onScrollChanged = { scrollY ->
                                viewModel.onPageChanged(
                                    uiState.currentPage,
                                    100,
                                    scrollY
                                )
                            }
                        )
                        BookFormat.TXT -> TxtReaderView(
                            filePath = book.filePath,
                            fontSize = uiState.fontSize,
                            onTextSelected = { viewModel.onTextSelected(it) }
                        )
                        BookFormat.UNKNOWN -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Formato não suportado")
                            }
                        }
                    }
                }

                // Dialog de definição de palavra
                if (uiState.wordDefinition != null || uiState.isSearchingWord || uiState.wordSearchError != null) {
                    WordDefinitionDialog(
                        word = uiState.selectedText,
                        definition = uiState.wordDefinition?.definition,
                        isLoading = uiState.isSearchingWord,
                        error = uiState.wordSearchError,
                        onDismiss = { viewModel.dismissWordDefinition() }
                    )
                }

                // Dialog de highlight
                if (uiState.showHighlightDialog) {
                    HighlightColorDialog(
                        text = uiState.selectedText,
                        onColorSelected = { color -> viewModel.saveHighlight(uiState.selectedText, color) },
                        onDismiss = { viewModel.showHighlightDialog(false) }
                    )
                }
            }
        }
    }
}

// ─── PDF Reader ───────────────────────────────────────────────────────────────

@Composable
fun PdfReaderView(
    filePath: String,
    initialPage: Int,
    onPageChanged: (Int, Int) -> Unit
) {
    val file = java.io.File(filePath)
    if (!file.exists()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Arquivo não encontrado")
        }
        return
    }

    var renderer: PdfRenderer? = null
    var totalPages = 0

    try {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(fd)
        totalPages = renderer.pageCount
    } catch (e: Exception) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro ao abrir PDF: ${e.message}")
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, (totalPages - 1).coerceAtLeast(0)),
        pageCount = { totalPages }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page, totalPages)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        AndroidView(
            factory = { ctx ->
                android.widget.ImageView(ctx).apply {
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView ->
                try {
                    val page = renderer?.openPage(pageIndex)
                    page?.let {
                        val width = imageView.width.takeIf { it > 0 } ?: 1080
                        val height = (width * it.height.toFloat() / it.width).toInt()
                        val bitmap = android.graphics.Bitmap.createBitmap(
                            width, height, android.graphics.Bitmap.Config.ARGB_8888
                        )
                        it.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        it.close()
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    // Página não pôde ser renderizada
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ─── Web Reader (EPUB e HTML) ─────────────────────────────────────────────────

@Composable
fun WebReaderView(
    filePath: String,
    format: BookFormat,
    fontSize: Float,
    onTextSelected: (String) -> Unit,
    onScrollChanged: (Int) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    textZoom = fontSize.toInt()
                }
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            val uri = Uri.fromFile(java.io.File(filePath))
            if (format == BookFormat.EPUB) {
                // Para EPUB: extrair conteúdo HTML e carregar
                // Implementação completa requer parser EPUB
                webView.loadUrl("file://$filePath")
            } else {
                webView.loadUrl(uri.toString())
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ─── TXT Reader ───────────────────────────────────────────────────────────────

@Composable
fun TxtReaderView(
    filePath: String,
    fontSize: Float,
    onTextSelected: (String) -> Unit
) {
    val content = try {
        java.io.File(filePath).readText()
    } catch (e: Exception) {
        "Erro ao ler arquivo: ${e.message}"
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = androidx.compose.ui.unit.TextUnit(
                            fontSize,
                            androidx.compose.ui.unit.TextUnitType.Sp
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Dialogs ──────────────────────────────────────────────────────────────────

@Composable
fun WordDefinitionDialog(
    word: String,
    definition: String?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(word) },
        text = {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                definition != null -> Text(definition)
                else -> Text("Sem resultado.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

@Composable
fun HighlightColorDialog(
    text: String,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color(0xFFFFD700) to "Amarelo",
        Color(0xFF90EE90) to "Verde",
        Color(0xFFFFB6C1) to "Rosa",
        Color(0xFF87CEEB) to "Azul"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha uma cor") },
        text = {
            Column {
                Text(
                    text = "\"${text.take(80)}${if (text.length > 80) "…" else ""}\"",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (color, name) ->
                        IconButton(
                            onClick = { onColorSelected(color.hashCode()) },
                            modifier = Modifier.background(color, androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Text(" ", color = Color.Transparent)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
