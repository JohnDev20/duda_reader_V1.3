package com.duda.app.core.di

import com.duda.app.data.repository.BookRepositoryImpl
import com.duda.app.data.repository.DictionaryRepositoryImpl
import com.duda.app.data.repository.HighlightRepositoryImpl
import com.duda.app.data.repository.TagRepositoryImpl
import com.duda.app.data.repository.VocabularyRepositoryImpl
import com.duda.app.domain.repository.BookRepository
import com.duda.app.domain.repository.DictionaryRepository
import com.duda.app.domain.repository.HighlightRepository
import com.duda.app.domain.repository.TagRepository
import com.duda.app.domain.repository.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindHighlightRepository(impl: HighlightRepositoryImpl): HighlightRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindDictionaryRepository(impl: DictionaryRepositoryImpl): DictionaryRepository
}
