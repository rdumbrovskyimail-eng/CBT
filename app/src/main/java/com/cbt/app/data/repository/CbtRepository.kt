package com.cbt.app.data.repository

import com.cbt.app.data.db.CbtNoteDao
import com.cbt.app.data.model.CbtNote
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для управления архивными КПТ-записями.
 * Обеспечивает единую точку доступа к данным, потокобезопасность
 * и реактивное обновление интерфейса.
 */
@Singleton
class CbtRepository @Inject constructor(
    private val dao: CbtNoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Поток всех активных записей блокнота, отсортированных
     * по закреплению и дате последнего редактирования.
     */
    val activeNotes: Flow<List<CbtNote>> = dao.getActiveNotes()
        .flowOn(ioDispatcher)

    /**
     * Поток архивных записей (для просмотра завершенных циклов рефлексии).
     */
    val archivedNotes: Flow<List<CbtNote>> = dao.getArchivedNotes()
        .flowOn(ioDispatcher)

    /**
     * Общее количество активных записей.
     */
    val activeNotesCount: Flow<Int> = dao.getActiveNotesCount()
        .flowOn(ioDispatcher)

    /**
     * Поиск по содержимому записей.
     */
    fun searchNotes(query: String): Flow<List<CbtNote>> {
        val sanitized = query.trim()
        return if (sanitized.isEmpty()) {
            activeNotes
        } else {
            dao.searchNotes(sanitized).flowOn(ioDispatcher)
        }
    }

    /**
     * Реактивное наблюдение за отдельной записью по ID.
     */
    fun getNoteByIdFlow(id: Long): Flow<CbtNote?> {
        return dao.getNoteByIdFlow(id).flowOn(ioDispatcher)
    }

    /**
     * Однократное получение записи по ID.
     */
    suspend fun getNoteById(id: Long): CbtNote? = withContext(ioDispatcher) {
        dao.getNoteById(id)
    }

    /**
     * Сохранение записи.
     * Если ID == 0, создается новая запись с метками создания и изменения.
     * Если ID > 0, обновляется существующая запись с обновлением updatedAt.
     */
    suspend fun saveNote(note: CbtNote): Long = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        if (note.id == 0L) {
            val freshNote = note.copy(
                createdAt = if (note.createdAt == 0L) now else note.createdAt,
                updatedAt = now
            )
            dao.insert(freshNote)
        } else {
            val updatedNote = note.copy(updatedAt = now)
            dao.update(updatedNote)
            note.id
        }
    }

    /**
     * Удаление записи по ID.
     */
    suspend fun deleteNote(id: Long): Boolean = withContext(ioDispatcher) {
        dao.deleteById(id) > 0
    }

    /**
     * Переключение статуса закрепления записи вверху списка.
     */
    suspend fun togglePin(id: Long, currentPinState: Boolean) = withContext(ioDispatcher) {
        dao.setPinnedStatus(id, !currentPinState)
    }

    /**
     * Архивирование или разархивирование записи.
     */
    suspend fun toggleArchive(id: Long, currentArchiveState: Boolean) = withContext(ioDispatcher) {
        dao.setArchivedStatus(id, !currentArchiveState)
    }
}