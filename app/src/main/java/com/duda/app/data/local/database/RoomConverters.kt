package com.duda.app.data.local.database

import androidx.room.TypeConverter
import com.duda.app.data.local.entity.BookCategory
import com.duda.app.data.local.entity.BookFormat

class RoomConverters {

    @TypeConverter
    fun fromBookCategory(category: BookCategory): String = category.name

    @TypeConverter
    fun toBookCategory(value: String): BookCategory = BookCategory.valueOf(value)

    @TypeConverter
    fun fromBookFormat(format: BookFormat): String = format.name

    @TypeConverter
    fun toBookFormat(value: String): BookFormat = BookFormat.valueOf(value)
}
