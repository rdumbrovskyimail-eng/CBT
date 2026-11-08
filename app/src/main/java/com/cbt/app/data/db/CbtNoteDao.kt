package com.cbt.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cbt.app.data.model.CbtNote
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с архивными записями КПТ.
 * Поддерживает реактивные потоки Flow для мгновенного обновления интерфейса.
 */
@Dao
interface CbtNoteDao {

    /**
     * Получение всех активных (не заархивированных) записей.
     * Закрепленные идут первыми, затем — самые свежие по времени изменения.
     */
    @Query("SELECT * FROM cbt_notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveNotes(): Flow<List<CbtNote>>

    /**
     * Получение всех записей, отправленных в архив.
     */
    @Query("SELECT * FROM cbt_notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<CbtNote>>

    /**
     * Поиск по содержимому и названию в активных записях.
     */
    @Query("""
        SELECT * FROM cbt_notes 
        WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<CbtNote>>

    /**
     * Получение конкретной записи по её ID (реактивный поток).
     */
    @Query("SELECT * FROM cbt_notes WHERE id = :id LIMIT 1")
    fun getNoteByIdFlow(id: Long): Flow<CbtNote?>

    /**
     * Однократное получение записи по ID.
     */
    @Query("SELECT * FROM cbt_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): CbtNote?

    /**
     * Вставка новой записи. Возвращает сгенерированный ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: CbtNote): Long

    /**
     * Обновление существующей записи.
     */
    @Update
    suspend fun update(note: CbtNote): Int

    /**
     * Удаление записи по идентификатору.
     */
    @Query("DELETE FROM cbt_notes WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /**
     * Удаление объекта записи.
     */
    @Delete
    suspend fun delete(note: CbtNote): Int

    /**
     * Переключение статуса архивирования записи.
     */
    @Query("UPDATE cbt_notes SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchivedStatus(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Переключение статуса закрепления записи.
     */
    @Query("UPDATE cbt_notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinnedStatus(id: Long, isPinned: Boolean)

    /**
     * Подсчет общего количества записей в блокноте.
     */
    @Query("SELECT COUNT(*) FROM cbt_notes WHERE isArchived = 0")
    fun getActiveNotesCount(): Flow<Int>
}