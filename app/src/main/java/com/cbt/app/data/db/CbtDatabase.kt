package com.cbt.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cbt.app.data.model.CbtNote

/**
 * Локальная база данных SQLite на базе AndroidX Room для автономного
 * и приватного хранения архивных записей КПТ на устройстве.
 */
@Database(
    entities = [CbtNote::class],
    version = 1,
    exportSchema = false
)
abstract class CbtDatabase : RoomDatabase() {

    /**
     * Предоставляет доступ к операциям с заметками.
     */
    abstract fun cbtNoteDao(): CbtNoteDao

    companion object {
        const val DATABASE_NAME = "cbt_notebook.db"
    }
}