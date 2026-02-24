package com.duda.app.presentation.importbook

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duda.app.core.util.FileUtils
import com.duda.app.data.local.entity.BookFormat
import com.duda.app.domain.model.Book
import com.duda.app.domain.usecase.ImportBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val isImporting: Boolean = false,
    val importedBooks: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val isComplete: Boolean = false
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importBookUseCase: ImportBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun importFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return

        _uiState.value = _uiState.value.copy(isImporting = true)

        viewModelScope.launch {
            val imported = mutableListOf<String>()
            val errors = mutableListOf<String>()

            uris.forEach { uri ->
                try {
                    val fileName = FileUtils.getFileNameFromUri(context, uri)
                        ?: "livro_${System.currentTimeMillis()}"

                    val filePath = FileUtils.copyUriToInternalStorage(context, uri)
                    if (filePath == null) {
                        errors.add("Falha ao copiar: $fileName")
                        return@forEach
                    }

                    val format = FileUtils.detectFormat(context, uri)
                    if (format == BookFormat.UNKNOWN) {
                        errors.add("Formato não suportado: $fileName")
                        FileUtils.deleteFile(filePath)
                        return@forEach
                    }

                    val title = FileUtils.extractTitleFromFileName(fileName)

                    val book = Book(
                        title = title,
                        author = "",
                        filePath = filePath,
                        format = format
                    )

                    importBookUseCase(book)
                    imported.add(title)

                } catch (e: Exception) {
                    errors.add("Erro: ${e.message}")
                }
            }

            _uiState.value = _uiState.value.copy(
                isImporting = false,
                importedBooks = imported,
                errors = errors,
                isComplete = true
            )
        }
    }
}

// ─── ImportScreen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: ImportViewModel = hiltViewModel(),
    onImportComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Mimetypes suportados
    val supportedMimeTypes = arrayOf(
        "application/pdf",
        "application/epub+zip",
        "text/plain",
        "text/html"
    )

    // Launcher para 1 arquivo
    val singleFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFiles(listOf(it)) }
    }

    // Launcher para múltiplos arquivos
    val multipleFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.importFiles(uris)
    }

    // Navegar de volta automaticamente após importação bem-sucedida
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete && uiState.errors.isEmpty() && uiState.importedBooks.isNotEmpty()) {
            onImportComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar livros") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isImporting) {
                CircularProgressIndicator()
                Text("Importando livros…")
            } else if (uiState.isComplete) {
                // Resultado
                if (uiState.importedBooks.isNotEmpty()) {
                    Text(
                        "✅ ${uiState.importedBooks.size} livro(s) importado(s) com sucesso!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.errors.isNotEmpty()) {
                    Text(
                        "⚠️ ${uiState.errors.size} erro(s):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    LazyColumn {
                        items(uiState.errors) { error ->
                            Text(error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Button(onClick = onImportComplete) {
                    Text("Ir para a biblioteca")
                }
            } else {
                // Estado inicial
                Text(
                    text = "Formatos suportados: PDF, EPUB, TXT, HTML",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Importar 1 livro
                OutlinedButton(
                    onClick = { singleFileLauncher.launch(supportedMimeTypes) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.FileOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar 1 livro")
                }

                // Importar em massa
                Button(
                    onClick = { multipleFilesLauncher.launch(supportedMimeTypes) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar vários livros")
                }
            }
        }
    }
}
