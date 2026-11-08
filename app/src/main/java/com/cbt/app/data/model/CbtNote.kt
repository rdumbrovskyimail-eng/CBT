package com.cbt.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Сущность архивной записи КПТ (когнитивно-поведенческой терапии).
 *
 * @param id Уникальный идентификатор записи.
 * @param title Пользовательский заголовок (если пустой, генерируется из текста).
 * @param content Основной текст записи/саморефлексии.
 * @param createdAt Временная метка создания записи.
 * @param updatedAt Временная метка последнего редактирования.
 * @param isPinned Закреплена ли запись вверху списка (для ключевых копинг-стратегий).
 * @param isArchived Находится ли запись в постоянном архиве.
 */
@Entity(tableName = "cbt_notes")
data class CbtNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
) {
    /**
     * Возвращает отображаемое название записи.
     * Если поле заголовка пустое, автоматически берется первая осмысленная строка
     * или первые слова из текста записи, как положено в эргономичном блокноте.
     */
    fun displayTitle(): String {
        if (title.isNotBlank()) {
            return title.trim()
        }
        val firstLine = content.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }

        return when {
            firstLine == null -> "Новая мысль"
            firstLine.length <= 48 -> firstLine
            else -> firstLine.take(45).trimEnd() + "..."
        }
    }

    /**
     * Возвращает краткий превью-фрагмент для карточки в архиве
     * (пропуская заголовочную строку, если заголовок был взят из текста).
     */
    fun previewSnippet(): String {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return "Пустая запись"

        val lines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val bodyLines = if (title.isBlank() && lines.size > 1) {
            lines.drop(1)
        } else {
            lines
        }

        val snippet = bodyLines.joinToString(" ")
        return if (snippet.length <= 110) snippet else snippet.take(107).trimEnd() + "..."
    }

    /**
     * Человекочитаемая дата для психологического комфорта при чтении.
     */
    fun formattedDate(): String {
        val formatter = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
        return formatter.format(Date(updatedAt))
    }

    /**
     * Краткая дата для карточки в списке.
     */
    fun shortDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yy", Locale("ru"))
        return formatter.format(Date(updatedAt))
    }
}