package com.cbt.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cbt.app.data.model.CbtNote
import com.cbt.app.ui.theme.*

/**
 * Главный экран блокнота: чистое белое полотно с архивным списком
 * и крупным плюсиком для быстрой фиксации КПТ-мысли.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<CbtNote>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (CbtNote) -> Unit,
    onAddClick: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onTogglePin: (Long, Boolean) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CbtBackgroundCanvas,
        topBar = {
            Surface(
                color = CbtPureWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CBT Блокнот",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CbtSagePrimary
                                )
                            )
                            Text(
                                text = if (notes.isEmpty()) "Чистое пространство" else "Записей в архиве: ${notes.size}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = CbtTextSecondary
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) onSearchQueryChange("")
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CbtSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = CbtSagePrimary
                            )
                        }
                    }

                    AnimatedVisibility(visible = isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Поиск по мыслям и ситуациям...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CbtSagePrimary,
                                unfocusedBorderColor = CbtBorderLight,
                                focusedContainerColor = CbtPureWhite,
                                unfocusedContainerColor = CbtPureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Большенький, комфортный для нажатия плюсик в шалфейном тоне
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = CbtSagePrimary,
                contentColor = CbtPureWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .size(68.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новая мысль",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (notes.isEmpty()) {
                // Психологически комфортное чистое белое полотно (Empty State)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CbtSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CbtSagePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Ничего не найдено" else "Ваш разум свободен",
                            style = MaterialTheme.typography.titleLarge.copy(color = CbtTextPrimary),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "Попробуйте изменить формулировку поискового запроса."
                            } else {
                                "Белое полотно готово для записи. Нажмите на крупный плюс внизу, чтобы зафиксировать и разобрать автоматическую мысль."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CbtTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        CbtNoteCard(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onDelete = { onDeleteNote(note.id) },
                            onTogglePin = { onTogglePin(note.id, note.isPinned) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка архивной записи КПТ.
 */
@Composable
fun CbtNoteCard(
    note: CbtNote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CbtPureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (note.isPinned) CbtBorderFocused else CbtBorderLight,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.displayTitle(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CbtTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Закрепить",
                            tint = if (note.isPinned) CbtSagePrimary else CbtTextPlaceholder,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить",
                            tint = CbtTextPlaceholder,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = note.previewSnippet(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CbtTextSecondary,
                    lineHeight = 20.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.shortDate(),
                    style = MaterialTheme.typography.labelSmall.copy(color = CbtTextPlaceholder)
                )

                if (note.isPinned) {
                    Text(
                        text = "Закреплено",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CbtSagePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}