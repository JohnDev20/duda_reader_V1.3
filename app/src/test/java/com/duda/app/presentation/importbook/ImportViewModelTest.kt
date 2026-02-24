package com.duda.app.presentation.importbook

import android.content.Context
import android.net.Uri
import com.duda.app.core.util.FileUtils
import com.duda.app.data.local.entity.BookFormat
import com.duda.app.domain.usecase.ImportBookUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var importBookUseCase: ImportBookUseCase

    private lateinit var viewModel: ImportViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        mockkObject(FileUtils)
        
        viewModel = ImportViewModel(context, importBookUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `importFiles with empty list should do nothing`() = runTest {
        // Act
        viewModel.importFiles(emptyList())
        
        // Assert
        assertThat(viewModel.uiState.value.isImporting).isFalse()
        coVerify(exactly = 0) { importBookUseCase(any()) }
    }

    @Test
    fun `importFiles should update uiState to complete when successful`() = runTest {
        // Arrange
        val uri = mockk<Uri>()
        val uris = listOf(uri)
        val fileName = "test_book.pdf"
        val filePath = "/internal/storage/test_book.pdf"
        val title = "test book"
        
        every { FileUtils.getFileNameFromUri(context, uri) } returns fileName
        every { FileUtils.copyUriToInternalStorage(context, uri) } returns filePath
        every { FileUtils.detectFormat(context, uri) } returns BookFormat.PDF
        every { FileUtils.extractTitleFromFileName(fileName) } returns title
        coEvery { importBookUseCase(any()) } returns 1L

        // Act
        viewModel.importFiles(uris)
        
        // Assert - Initial state check
        assertThat(viewModel.uiState.value.isImporting).isTrue()
        
        // Advance time to complete coroutine
        advanceUntilIdle()

        // Assert - Final state check
        val state = viewModel.uiState.value
        assertThat(state.isImporting).isFalse()
        assertThat(state.isComplete).isTrue()
        assertThat(state.importedBooks).contains(title)
        assertThat(state.errors).isEmpty()
        
        coVerify(exactly = 1) { importBookUseCase(any()) }
    }

    @Test
    fun `importFiles should record error when copy fails`() = runTest {
        // Arrange
        val uri = mockk<Uri>()
        val uris = listOf(uri)
        
        every { FileUtils.getFileNameFromUri(context, uri) } returns "book.pdf"
        every { FileUtils.copyUriToInternalStorage(context, uri) } returns null // Failure

        // Act
        viewModel.importFiles(uris)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isComplete).isTrue()
        assertThat(state.errors).isNotEmpty()
        assertThat(state.errors[0]).contains("Falha ao copiar")
    }

    @Test
    fun `importFiles should record error when format is unknown`() = runTest {
        // Arrange
        val uri = mockk<Uri>()
        val uris = listOf(uri)
        val fileName = "unknown.xyz"
        val filePath = "/internal/storage/unknown.xyz"
        
        every { FileUtils.getFileNameFromUri(context, uri) } returns fileName
        every { FileUtils.copyUriToInternalStorage(context, uri) } returns filePath
        every { FileUtils.detectFormat(context, uri) } returns BookFormat.UNKNOWN
        every { FileUtils.deleteFile(filePath) } returns true

        // Act
        viewModel.importFiles(uris)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isComplete).isTrue()
        assertThat(state.importedBooks).isEmpty()
        assertThat(state.errors).isNotEmpty()
        assertThat(state.errors[0]).contains("Formato não suportado")
        
        coVerify(exactly = 0) { importBookUseCase(any()) }
    }

    @Test
    fun `importFiles should handle generic exception and record it`() = runTest {
        // Arrange
        val uri = mockk<Uri>()
        every { FileUtils.getFileNameFromUri(context, uri) } throws RuntimeException("Crash!")

        // Act
        viewModel.importFiles(listOf(uri))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.errors).isNotEmpty()
        assertThat(state.errors[0]).contains("Erro: Crash!")
    }
}
