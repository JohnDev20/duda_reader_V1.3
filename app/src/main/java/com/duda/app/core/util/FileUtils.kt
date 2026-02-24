package com.duda.app.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.duda.app.data.local.entity.BookFormat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileUtils {

    private const val BOOKS_DIR = "books"
    private const val COVERS_DIR = "covers"

    /**
     * Copia um arquivo URI (Scoped Storage) para o armazenamento interno privado do app.
     * Retorna o caminho do arquivo copiado ou null em caso de erro.
     *
     * Necessário porque a URI do File Picker pode se tornar inválida após o processo ser encerrado.
     */
    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val fileName = getFileNameFromUri(context, uri) ?: generateFileName(uri)
            val booksDir = File(context.filesDir, BOOKS_DIR).apply { mkdirs() }
            val destFile = File(booksDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtém o nome do arquivo original a partir da URI.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Detecta o formato do livro pelo MIME type ou extensão do arquivo.
     */
    fun detectFormat(context: Context, uri: Uri): BookFormat {
        // Tenta pelo MIME type primeiro
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            val formatByMime = BookFormat.fromMimeType(mimeType)
            if (formatByMime != BookFormat.UNKNOWN) return formatByMime
        }

        // Fallback pela extensão do nome do arquivo
        val fileName = getFileNameFromUri(context, uri) ?: return BookFormat.UNKNOWN
        val extension = fileName.substringAfterLast('.', "")
        return BookFormat.fromExtension(extension)
    }

    /**
     * Detecta o formato pelo caminho do arquivo já copiado.
     */
    fun detectFormatFromPath(filePath: String): BookFormat {
        val extension = filePath.substringAfterLast('.', "")
        return BookFormat.fromExtension(extension)
    }

    /**
     * Extrai o título do livro a partir do nome do arquivo (sem extensão).
     */
    fun extractTitleFromFileName(fileName: String): String {
        return fileName
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
    }

    /**
     * Verifica se um arquivo interno ainda existe.
     */
    fun fileExists(filePath: String): Boolean = File(filePath).exists()

    /**
     * Deleta um arquivo do armazenamento interno.
     */
    fun deleteFile(filePath: String): Boolean = File(filePath).delete()

    /**
     * Gera um nome de arquivo único baseado na URI e timestamp.
     */
    private fun generateFileName(uri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val extension = uri.lastPathSegment?.substringAfterLast('.') ?: "bin"
        return "book_${timestamp}.$extension"
    }

    /**
     * Retorna o diretório interno de livros.
     */
    fun getBooksDir(context: Context): File =
        File(context.filesDir, BOOKS_DIR).apply { mkdirs() }
}
