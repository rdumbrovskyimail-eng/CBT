package com.cbt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbt.app.ui.MainViewModel
import com.cbt.app.ui.display.DisplayRateManager
import com.cbt.app.ui.editor.CbtEditorScreen
import com.cbt.app.ui.home.HomeScreen
import com.cbt.app.ui.theme.CbtBackgroundCanvas
import com.cbt.app.ui.theme.CbtTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка безрамочного светлого режима (Edge-to-Edge)
        // Статус-бар и панель навигации прозрачны, иконки темные (контрастные)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Активируем плавные 120 Гц для предотвращения зрительной усталости
        DisplayRateManager.setHighRefreshRate(window, true)

        setContent {
            CbtTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CbtBackgroundCanvas
                ) {
                    val notes by viewModel.notes.collectAsStateWithLifecycle()
                    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                    val editorState by viewModel.editorState.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Главное белое полотно со списком записей и большим плюсиком
                        HomeScreen(
                            notes = notes,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onNoteClick = { note -> viewModel.openExistingNote(note) },
                            onAddClick = { viewModel.openNewNote() },
                            onDeleteNote = { id -> viewModel.deleteNote(id) },
                            onTogglePin = { id, currentPin -> viewModel.togglePin(id, currentPin) }
                        )

                        // Полное окно КПТ-блокнота: плавный выезд, режим комфортного чтения
                        // по умолчанию, быстрый переход к редактированию по тапу и панель действий
                        AnimatedVisibility(
                            visible = editorState.isOpen,
                            enter = fadeIn() + slideInVertically { fullHeight -> fullHeight / 8 },
                            exit = fadeOut() + slideOutVertically { fullHeight -> fullHeight / 8 }
                        ) {
                            CbtEditorScreen(
                                state = editorState,
                                onClose = { viewModel.closeEditor() },
                                onStartEditing = { viewModel.startEditing() },
                                onStopEditing = { viewModel.stopEditing() },
                                onTitleChange = { viewModel.onTitleChanged(it) },
                                onContentChange = { viewModel.onContentChanged(it) },
                                onPasteText = { viewModel.pasteTextIntoContent(it) },
                                onClearContent = { viewModel.clearContent() },
                                onOpenReplaceDialog = { viewModel.setReplaceDialogVisible(it) },
                                onPerformReplace = { find, replace -> viewModel.performReplace(find, replace) },
                                onDeleteCurrent = {
                                    editorState.noteId?.let { id -> viewModel.deleteNote(id) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        DisplayRateManager.setHighRefreshRate(window, true)
    }

    override fun onPause() {
        super.onPause()
        DisplayRateManager.setHighRefreshRate(window, false)
    }
}