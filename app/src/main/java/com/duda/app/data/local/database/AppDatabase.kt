package com.duda.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.duda.app.data.local.dao.BookDao
import com.duda.app.data.local.dao.HighlightDao
import com.duda.app.data.local.dao.TagDao
import com.duda.app.data.local.dao.VocabularyDao
import com.duda.app.data.local.entity.BookEntity
import com.duda.app.data.local.entity.BookTagCrossRef
import com.duda.app.data.local.entity.HighlightEntity
import com.duda.app.data.local.entity.TagEntity
import com.duda.app.data.local.entity.VocabularyEntity
import com.duda.app.data.local.migration.Migrations

@Database(
    entities = [
        BookEntity::class,
        HighlightEntity::class,
        VocabularyEntity::class,
        TagEntity::class,
        BookTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun highlightDao(): HighlightDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "duda_database"

        /**
         * Lista de migrations. Adicione aqui quando incrementar a versão do banco.
         * Exemplo de uso na próxima versão:
         *   val MIGRATION_1_2 = Migrations.migration1To2
         */
        val ALL_MIGRATIONS = arrayOf(
            // Migrations.migration1To2  // descomente na versão 2
        )
    }
}
