package com.cbt.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cbt.app.ui.EditorUiState
import com.cbt.app.ui.theme.*

/**
 * Окно полного размера для комфортного чтения и редактирования КПТ-записи:
 * - По умолчанию открывается в РЕЖИМЕ ЧТЕНИЯ (психологический уют, без клавиатуры).
 * - Тап по экрану включает режим редактирования и плавно поднимает окно над клавиатурой (imePadding).
 * - Панель быстрых минималистичных инструментов: Вставить, Скопировать, Заменить, Очистить.
 */
@Composable
fun CbtEditorScreen(
    state: EditorUiState,
    onClose: () -> Unit,
    onStartEditing: () -> Unit,
    onStopEditing: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPasteText: (String) -> Unit,
    onClearContent: () -> Unit,
    onOpenReplaceDialog: (Boolean) -> Unit,
    onPerformReplace: (String, String) -> Unit,
    onDeleteCurrent: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val contentFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // Системная кнопка "Назад" плавно сохраняет и закрывает редактор
    BackHandler {
        onClose()
    }

    LaunchedEffect(state.isEditing) {
        if (state.isEditing) {
            contentFocusRequester.requestFocus()
        }
    }

    Surface(
        color = CbtPureWhite,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            // Обеспечивает поднятие окна строго над всплывающей клавиатурой с сохранением простора
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // =========================================================================
            // Верхняя минималистичная панель навигации
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Сохранить и закрыть",
                        tint = CbtSagePrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (state.isEditing) {
                        // Кнопка переключения обратно в режим спокойного чтения
                        TextButton(
                            onClick = onStopEditing,
                            colors = ButtonDefaults.textButtonColors(contentColor = CbtSagePrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Готово", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (state.noteId != null) {
                        IconButton(onClick = onDeleteCurrent) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Удалить запись",
                                tint = CbtDangerSoft
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CbtBorderLight, thickness = 1.dp)

            // =========================================================================
            // Панель быстрых минималистичных инструментов (Вставить, Скопировать, Заменить, Очистить)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CbtSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Вставить
                AssistChip(
                    onClick = {
                        val clipText = clipboardManager.getText()?.text.orEmpty()
                        onPasteText(clipText)
                    },
                    label = { Text("Вставить", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CbtPureWhite,
                        labelColor = CbtSagePrimary
                    ),
                    border = BorderStroke(1.dp, CbtBorderLight)
                )

                // Скопировать
                AssistChip(
                    onClick = {
                        val textToCopy = if (state.titleInputValue.isNotBlank()) {
                            "${state.titleInputValue}\n\n${state.contentInputValue}"
                        } else {
                            state.contentInputValue
                        }
                        clipboardManager.setText(AnnotatedString(textToCopy))
                    },
                    label = { Text("Копировать", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CbtPureWhite,
                        labelColor = CbtSageSecondary
                    ),
                    border = BorderStroke(1.dp, CbtBorderLight)
                )

                // Заменить
                AssistChip(
                    onClick = { onOpenReplaceDialog(true) },
                    label = { Text("Заменить", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CbtPureWhite,
                        labelColor = CbtSageSecondary
                    ),
                    border = BorderStroke(1.dp, CbtBorderLight)
                )

                // Очистить
                AssistChip(
                    onClick = onClearContent,
                    label = { Text("Очистить", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CbtPureWhite,
                        labelColor = CbtTextSecondary
                    ),
                    border = BorderStroke(1.dp, CbtBorderLight)
                )
            }

            HorizontalDivider(color = CbtBorderLight, thickness = 0.5.dp)

            // =========================================================================
            // Основное полотно: Чтение (по умолчанию) или Редактирование (по тапу)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Клик по полотну переводит блокнот в режим ввода
                        if (!state.isEditing) onStartEditing()
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ПОЛЕ ЗАГОЛОВКА
                    if (state.isEditing) {
                        TextField(
                            value = state.titleInputValue,
                            onValueChange = onTitleChange,
                            placeholder = {
                                Text(
                                    text = "Название (или возьмется из текста)",
                                    style = MaterialTheme.typography.titleLarge.copy(color = CbtTextPlaceholder)
                                )
                            },
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CbtTextPrimary
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Режим комфортного чтения заголовка
                        val displayTitle = when {
                            state.titleInputValue.isNotBlank() -> state.titleInputValue
                            state.contentInputValue.isNotBlank() -> {
                                val first = state.contentInputValue.lines().firstOrNull { it.isNotBlank() } ?: "Запись КПТ"
                                if (first.length <= 48) first else first.take(45) + "..."
                            }
                            else -> "Новая мысль"
                        }

                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CbtTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Режим спокойного чтения • Тапните по тексту для правок",
                            style = MaterialTheme.typography.labelSmall.copy(color = CbtSagePrimary)
                        )
                    }

                    HorizontalDivider(color = CbtBorderLight.copy(alpha = 0.5f), thickness = 1.dp)

                    // ПОЛЕ ОСНОВНОГО ТЕКСТА
                    if (state.isEditing) {
                        TextField(
                            value = state.contentInputValue,
                            onValueChange = onContentChange,
                            placeholder = {
                                Text(
                                    text = "Опишите ситуацию, возникшую автоматическую мысль, эмоцию или рациональный ответ...",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = CbtTextPlaceholder)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = CbtTextPrimary,
                                lineHeight = 28.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 350.dp)
                                .focusRequester(contentFocusRequester)
                        )
                    } else {
                        // Режим идеального психологического чтения без элементов ввода
                        val displayText = state.contentInputValue.ifBlank {
                            "Текст записи пока пуст. Коснитесь этой зоны, чтобы начать ввод мыслей."
                        }

                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (state.contentInputValue.isBlank()) CbtTextPlaceholder else CbtTextPrimary,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // Минималистичный диалог «Найти и заменить»
    // =========================================================================
    if (state.showReplaceDialog) {
        var findQuery by remember { mutableStateOf("") }
        var replaceQuery by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { onOpenReplaceDialog(false) }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CbtPureWhite,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Заменить в тексте",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CbtSagePrimary)
                    )

                    OutlinedTextField(
                        value = findQuery,
                        onValueChange = { findQuery = it },
                        label = { Text("Что найти") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CbtSagePrimary,
                            focusedLabelColor = CbtSagePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        label = { Text("На что заменить") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CbtSagePrimary,
                            focusedLabelColor = CbtSagePrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onOpenReplaceDialog(false) }) {
                            Text("Отмена", color = CbtTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onPerformReplace(findQuery, replaceQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = CbtSagePrimary)
                        ) {
                            Text("Заменить все")
                        }
                    }
                }
            }
        }
    }
}