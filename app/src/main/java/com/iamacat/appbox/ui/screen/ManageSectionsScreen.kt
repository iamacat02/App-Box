package com.iamacat.appbox.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.ui.component.DeleteSectionDialog
import com.iamacat.appbox.ui.component.DeleteSectionsDialog
import com.iamacat.appbox.ui.component.EmptySectionState
import com.iamacat.appbox.ui.component.LauncherToolbar
import com.iamacat.appbox.ui.component.SectionFormDialog
import com.iamacat.appbox.ui.component.SectionIcons
import com.iamacat.appbox.ui.component.SelectSectionDialog
import com.iamacat.appbox.ui.component.rememberReorderState
import com.iamacat.appbox.ui.event.ManageSectionsEffect
import com.iamacat.appbox.ui.event.ManageSectionsUiEvent
import com.iamacat.appbox.ui.state.ManageSectionsUiState
import com.iamacat.appbox.ui.viewmodel.ManageSectionsViewModel

@Composable
fun ManageSectionsScreen(
    viewModel: ManageSectionsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ManageSectionsEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ManageSectionsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@Composable
private fun ManageSectionsContent(
    uiState: ManageSectionsUiState,
    onEvent: (ManageSectionsUiEvent) -> Unit,
    onBack: () -> Unit
) {
    var menuSectionId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val reorderState = rememberReorderState(listState) { fromIndex, toIndex ->
        uiState.sections.getOrNull(fromIndex)?.let { section ->
            onEvent(ManageSectionsUiEvent.MoveSection(section.id, toIndex))
        }
    }

    BackHandler(enabled = uiState.selectionMode) {
        onEvent(ManageSectionsUiEvent.ExitSelection)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LauncherToolbar(
                title = if (uiState.selectionMode) {
                    "${uiState.selectedSectionIds.size} selected"
                } else {
                    "Manage Sections"
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.selectionMode) {
                                onEvent(ManageSectionsUiEvent.ExitSelection)
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.selectionMode) {
                                Icons.Filled.Close
                            } else {
                                Icons.AutoMirrored.Filled.ArrowBack
                            },
                            contentDescription = if (uiState.selectionMode) "Exit selection" else "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.selectionMode) {
                        IconButton(
                            onClick = { onEvent(ManageSectionsUiEvent.RequestBulkDelete) },
                            enabled = uiState.selectedSectionIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete selected sections"
                            )
                        }
                    } else {
                        IconButton(onClick = { onEvent(ManageSectionsUiEvent.AddSectionClicked) }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Create section"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )

                uiState.sections.isEmpty() -> EmptySectionState(
                    title = "No sections yet",
                    message = "Use the + button to create your first section"
                )

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = uiState.sections,
                        key = { _, section -> section.id }
                    ) { _, section ->
                        ManageSectionRow(
                            section = section,
                            selectionMode = uiState.selectionMode,
                            selected = section.id in uiState.selectedSectionIds,
                            isDragging = reorderState.isDragging(section.id),
                            dragOffset = reorderState.offsetFor(section.id),
                            menuExpanded = menuSectionId == section.id,
                            onToggleSelected = {
                                onEvent(ManageSectionsUiEvent.ToggleSelection(section.id))
                            },
                            onLongPress = {
                                menuSectionId = null
                                onEvent(ManageSectionsUiEvent.StartSelection(section.id))
                            },
                            onMenuClick = { menuSectionId = section.id },
                            onMenuDismiss = { menuSectionId = null },
                            onEdit = {
                                menuSectionId = null
                                onEvent(ManageSectionsUiEvent.EditSection(section))
                            },
                            onSetDefault = {
                                menuSectionId = null
                                onEvent(ManageSectionsUiEvent.ToggleDefault(section.id))
                            },
                            onToggleVisibility = {
                                menuSectionId = null
                                onEvent(ManageSectionsUiEvent.ToggleVisibility(section.id))
                            },
                            onDelete = {
                                menuSectionId = null
                                onEvent(ManageSectionsUiEvent.RequestDelete(section))
                            },
                            onDragStart = { reorderState.onDragStart(section.id) },
                            onDrag = { delta -> reorderState.onDrag(delta) },
                            onDragEnd = { reorderState.onDragEnd() },
                            modifier = Modifier.zIndex(if (reorderState.isDragging(section.id)) 1f else 0f)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }

    if (uiState.showSelectTypeDialog) {
        SelectSectionDialog(
            onTypeSelected = { onEvent(ManageSectionsUiEvent.SectionTypeSelected(it)) },
            onDismiss = { onEvent(ManageSectionsUiEvent.DismissTypeDialog) }
        )
    }

    uiState.sectionDialog?.let { form ->
        SectionFormDialog(
            state = form,
            onNameChanged = { onEvent(ManageSectionsUiEvent.SectionNameChanged(it)) },
            onColorSelected = { onEvent(ManageSectionsUiEvent.SectionColorChanged(it)) },
            onDefaultChanged = { onEvent(ManageSectionsUiEvent.DialogDefaultChanged(it)) },
            onVisibleChanged = { onEvent(ManageSectionsUiEvent.DialogVisibilityChanged(it)) },
            onDelete = if (form.isEditing) {
                {
                    uiState.sections
                        .firstOrNull { it.id == form.editingSectionId }
                        ?.let { onEvent(ManageSectionsUiEvent.RequestDelete(it)) }
                }
            } else {
                null
            },
            onConfirm = { onEvent(ManageSectionsUiEvent.SaveSection) },
            onDismiss = { onEvent(ManageSectionsUiEvent.DismissSectionDialog) }
        )
    }

    uiState.pendingDelete?.let { section ->
        DeleteSectionDialog(
            sectionName = section.name,
            onConfirm = { onEvent(ManageSectionsUiEvent.ConfirmDelete) },
            onDismiss = { onEvent(ManageSectionsUiEvent.DismissDelete) }
        )
    }

    if (uiState.showBulkDeleteDialog) {
        DeleteSectionsDialog(
            count = uiState.selectedSectionIds.size,
            onConfirm = { onEvent(ManageSectionsUiEvent.ConfirmBulkDelete) },
            onDismiss = { onEvent(ManageSectionsUiEvent.DismissBulkDelete) }
        )
    }
}

@Composable
private fun ManageSectionRow(
    section: AppSection,
    selectionMode: Boolean,
    selected: Boolean,
    isDragging: Boolean,
    dragOffset: Float,
    menuExpanded: Boolean,
    onToggleSelected: () -> Unit,
    onLongPress: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = if (selectionMode) {
        Modifier.combinedClickable(onClick = onToggleSelected)
    } else {
        Modifier.pointerInput(section.id) {
            detectTapGestures(onLongPress = { onLongPress() })
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = dragOffset }
            .background(
                when {
                    isDragging -> MaterialTheme.colorScheme.surfaceVariant
                    selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .then(interaction)
            .heightIn(min = 56.dp)
            .padding(end = 8.dp)
    ) {
        if (selectionMode) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                Checkbox(checked = selected, onCheckedChange = null)
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .pointerInput(section.id) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Reorder ${section.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = SectionIcons.iconFor(section.type),
            contentDescription = null,
            tint = Color(section.colorArgb),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = section.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val status = buildList {
                if (section.isDefault) add("Default")
                if (!section.isVisible) add("Hidden")
            }
            if (status.isNotEmpty()) {
                Text(
                    text = status.joinToString(" \u00B7 "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!selectionMode) {
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Options for ${section.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = onMenuDismiss) {
                    DropdownMenuItem(
                        text = { Text("Edit Section") },
                        onClick = onEdit
                    )
                    DropdownMenuItem(
                        text = { Text(if (section.isDefault) "Default Section" else "Set as Default") },
                        enabled = !section.isDefault,
                        onClick = onSetDefault
                    )
                    DropdownMenuItem(
                        text = { Text(if (section.isVisible) "Hide Section" else "Show Section") },
                        onClick = onToggleVisibility
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Section", color = MaterialTheme.colorScheme.error) },
                        onClick = onDelete
                    )
                }
            }
        }
    }
}
