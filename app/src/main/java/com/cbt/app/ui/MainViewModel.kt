package com.cbt.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbt.app.data.model.CbtNote
import com.cbt.app.data.repository.CbtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Состояние экрана просмотра/редактирования записи.
 *
 * @param isOpen открыто ли сейчас модальное окно блокнота.
 * @param isEditing false = режим комфортного чтения; true = режим активного ввода (клавиатура, фокус).
 * @param noteId идентификатор открытой заметки (null — для новой).
 * @param titleInputValue текст в поле названия.
 * @param contentInputValue основной текст записи.
 * @param createdAt дата создания.
 * @param updatedAt дата последнего изменения.
 * @param isPinned статус закрепления.
 * @param showReplaceDialog открыто ли минималистичное диалоговое окно «Найти и заменить».
 */
data class EditorUiState(
    val isOpen: Boolean = false,
    val isEditing: Boolean = false,
    val noteId: Long? = null,
    val titleInputValue: String = "",
    val contentInputValue: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isPinned: Boolean = false,
    val showReplaceDialog: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CbtRepository
) : ViewModel() {

    // Строка быстрого поиска по архивным записям
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Состояние окна чтения/редактирования
    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    // Список активных заметок с учетом поискового фильтра
    val notes: StateFlow<List<CbtNote>> = repository.activeNotes
        .combine(_searchQuery) { allNotes, query ->
            val q = query.trim()
            if (q.isEmpty()) {
                allNotes
            } else {
                allNotes.filter { note ->
                    note.title.contains(q, ignoreCase = true) ||
                    note.content.contains(q, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // =========================================================================
    // Управление окном блокнота: открытие, режимы, сохранение
    // =========================================================================

    /**
     * Создание новой записи:
     * Открывается окно, готовое к набору текста по первому тапу.
     */
    fun openNewNote() {
        _editorState.value = EditorUiState(
            isOpen = true,
            isEditing = true, // Для новой записи сразу активируем редактирование
            noteId = null,
            titleInputValue = "",
            contentInputValue = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Открытие существующей записи:
     * Первоначально открывается в РЕЖИМЕ КОМФОРТНОГО ЧТЕНИЯ (isEditing = false).
     * Никаких раздражающих курсоров и скачущих клавиатур, пока пользователь не коснется текста.
     */
    fun openExistingNote(note: CbtNote) {
        _editorState.value = EditorUiState(
            isOpen = true,
            isEditing = false, // Режим спокойного чтения КПТ-протокола
            noteId = note.id,
            titleInputValue = note.title,
            contentInputValue = note.content,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            isPinned = note.isPinned
        )
    }

    /**
     * Пользователь нажал на текст в режиме чтения -> плавный переход в режим редактирования.
     */
    fun startEditing() {
        _editorState.update { it.copy(isEditing = true) }
    }

    /**
     * Возврат в режим чтения из режима редактирования.
     */
    fun stopEditing() {
        saveCurrentNoteInternal()
        _editorState.update { it.copy(isEditing = false) }
    }

    fun onTitleChanged(newTitle: String) {
        _editorState.update { it.copy(titleInputValue = newTitle) }
    }

    fun onContentChanged(newContent: String) {
        _editorState.update { it.copy(contentInputValue = newContent) }
    }

    /**
     * Закрытие окна с автоматическим сохранением изменений, если текст не пустой.
     */
    fun closeEditor() {
        val state = _editorState.value
        if (state.contentInputValue.isNotBlank() || state.titleInputValue.isNotBlank()) {
            saveCurrentNoteInternal()
        }
        _editorState.value = EditorUiState(isOpen = false)
    }

    /**
     * Внутреннее сохранение записи:
     * Если имя не задано, заголовок будет вычислен автоматически из текста.
     */
    private fun saveCurrentNoteInternal() {
        val state = _editorState.value
        val rawContent = state.contentInputValue
        val rawTitle = state.titleInputValue.trim()

        // Не создаем абсолютно пустые записи в архиве
        if (rawContent.isBlank() && rawTitle.isBlank()) return

        val finalTitle = if (rawTitle.isEmpty()) {
            // Если имя пустое — извлекаем первую строку текста для названия
            val firstLine = rawContent.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() } ?: "Запись"
            if (firstLine.length <= 48) firstLine else firstLine.take(45).trimEnd() + "..."
        } else {
            rawTitle
        }

        viewModelScope.launch {
            val noteToSave = CbtNote(
                id = state.noteId ?: 0L,
                title = finalTitle,
                content = rawContent,
                createdAt = if (state.createdAt != 0L) state.createdAt else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isPinned = state.isPinned,
                isArchived = false
            )
            val newId = repository.saveNote(noteToSave)
            _editorState.update { it.copy(noteId = newId, titleInputValue = finalTitle) }
        }
    }

    // =========================================================================
    // Инструменты редактирования (Вставить, Очистить, Заменить, Скопировать)
    // =========================================================================

    /**
     * Вставка текста из буфера обмена в текущий блокнот.
     */
    fun pasteTextIntoContent(clipboardText: String) {
        if (clipboardText.isEmpty()) return
        _editorState.update { current ->
            val updated = if (current.contentInputValue.isEmpty()) {
                clipboardText
            } else {
                current.contentInputValue + "\n" + clipboardText
            }
            current.copy(contentInputValue = updated, isEditing = true)
        }
    }

    /**
     * Полная очистка поля текста записи.
     */
    fun clearContent() {
        _editorState.update { it.copy(contentInputValue = "", isEditing = true) }
    }

    /**
     * Открытие/закрытие диалога «Заменить».
     */
    fun setReplaceDialogVisible(visible: Boolean) {
        _editorState.update { it.copy(showReplaceDialog = visible) }
    }

    /**
     * Поиск и замена фрагмента в тексте записи.
     */
    fun performReplace(target: String, replacement: String) {
        if (target.isEmpty()) return
        _editorState.update { current ->
            val newContent = current.contentInputValue.replace(target, replacement, ignoreCase = true)
            current.copy(
                contentInputValue = newContent,
                showReplaceDialog = false,
                isEditing = true
            )
        }
    }

    // =========================================================================
    // Операции с архивом
    // =========================================================================

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            if (_editorState.value.noteId == id) {
                _editorState.value = EditorUiState(isOpen = false)
            }
        }
    }

    fun togglePin(id: Long, currentPinState: Boolean) {
        viewModelScope.launch {
            repository.togglePin(id, currentPinState)
        }
    }

    fun toggleArchive(id: Long, currentArchiveState: Boolean) {
        viewModelScope.launch {
            repository.toggleArchive(id, currentArchiveState)
            if (_editorState.value.noteId == id) {
                _editorState.value = EditorUiState(isOpen = false)
            }
        }
    }
}