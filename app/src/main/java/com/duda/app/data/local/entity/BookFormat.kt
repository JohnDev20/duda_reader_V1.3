package com.duda.app.data.local.entity

enum class BookFormat {
    PDF,
    EPUB,
    TXT,
    HTML,
    UNKNOWN;

    companion object {
        fun fromExtension(extension: String): BookFormat {
            return when (extension.lowercase()) {
                "pdf" -> PDF
                "epub" -> EPUB
                "txt" -> TXT
                "html", "htm" -> HTML
                else -> UNKNOWN
            }
        }

        fun fromMimeType(mimeType: String): BookFormat {
            return when (mimeType.lowercase()) {
                "application/pdf" -> PDF
                "application/epub+zip" -> EPUB
                "text/plain" -> TXT
                "text/html" -> HTML
                else -> UNKNOWN
            }
        }
    }
}
