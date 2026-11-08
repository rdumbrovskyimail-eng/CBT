package com.cbt.app.di

import android.content.Context
import androidx.room.Room
import com.cbt.app.data.db.CbtDatabase
import com.cbt.app.data.db.CbtNoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Hilt-модуль, предоставляющий зависимости уровня базы данных Room
 * и потоков ввода-вывода для всего приложения.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CbtDatabase {
        return Room.databaseBuilder(
            context,
            CbtDatabase::class.java,
            CbtDatabase.DATABASE_NAME
        )
            // При необходимости мягко обновляем схему без падений
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCbtNoteDao(database: CbtDatabase): CbtNoteDao {
        return database.cbtNoteDao()
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}