package com.duda.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["title"]),
        Index(value = ["author"]),
        Index(value = ["category"]),
        Index(value = ["format"])
    ]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "cover_path") val coverPath: String? = null,
    val format: BookFormat,
    val category: BookCategory = BookCategory.NEW,
    @ColumnInfo(name = "last_page_read") val lastPageRead: Int = 0,
    @ColumnInfo(name = "reading_progress_percent") val readingProgressPercent: Float = 0f,
    @ColumnInfo(name = "scroll_position_y") val scrollPositionY: Int = 0,
    @ColumnInfo(name = "total_pages") val totalPages: Int = 0,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"])]
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    val text: String,
    val note: String = "",
    val color: Int = 0xFFFFD700.toInt(),
    @ColumnInfo(name = "page_number") val pageNumber: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "vocabulary",
    indices = [
        Index(value = ["word"]),
        Index(value = ["book_id"])
    ]
)
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val definition: String,
    @ColumnInfo(name = "book_id") val bookId: Long? = null,
    @ColumnInfo(name = "page_number") val pageNumber: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int = 0xFF6200EE.toInt()
)

@Entity(
    tableName = "book_tags",
    primaryKeys = ["book_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["tag_id"])
    ]
)
data class BookTagCrossRef(
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "tag_id") val tagId: Long
)
