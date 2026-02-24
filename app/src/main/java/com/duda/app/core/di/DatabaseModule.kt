package com.duda.app.core.di

import android.content.Context
import androidx.room.Room
import com.duda.app.data.local.dao.BookDao
import com.duda.app.data.local.dao.HighlightDao
import com.duda.app.data.local.dao.TagDao
import com.duda.app.data.local.dao.VocabularyDao
import com.duda.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .build()
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao = database.bookDao()

    @Provides
    fun provideHighlightDao(database: AppDatabase): HighlightDao = database.highlightDao()

    @Provides
    fun provideVocabularyDao(database: AppDatabase): VocabularyDao = database.vocabularyDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()
}
