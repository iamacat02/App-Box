package com.iamacat.appbox.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamacat.appbox.R
import com.iamacat.appbox.ui.component.AppContextMenu
import com.iamacat.appbox.ui.component.AppListItem
import com.iamacat.appbox.ui.component.AboutDialog
import com.iamacat.appbox.ui.component.ConfirmRemovalDialog
import com.iamacat.appbox.ui.component.EmptySectionState
import com.iamacat.appbox.ui.component.LauncherFab
import com.iamacat.appbox.ui.component.LauncherToolbar
import com.iamacat.appbox.ui.component.SectionFormDialog
import com.iamacat.appbox.ui.component.SectionHeader
import com.iamacat.appbox.ui.component.SectionPickerDialog
import com.iamacat.appbox.ui.component.SectionTabRow
import com.iamacat.appbox.ui.component.SelectSectionDialog
import com.iamacat.appbox.ui.component.SortAppsDialog
import com.iamacat.appbox.ui.event.MainEffect
import com.iamacat.appbox.ui.event.MainUiEvent
import com.iamacat.appbox.ui.state.MainUiState
import com.iamacat.appbox.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToAddApps: (String) -> Unit,
    onNavigateToManageSections: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MainEffect.NavigateToAddApps -> onNavigateToAddApps(effect.sectionId)
                MainEffect.NavigateToManageSections -> onNavigateToManageSections()
                MainEffect.NavigateToSettings -> onNavigateToSettings()
                is MainEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    MainContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    onEvent: (MainUiEvent) -> Unit
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    val swipeThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    val sections = uiState.sections
    val selectedSectionId = uiState.selectedSectionId
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column {
                LauncherToolbar(
                    title = "App Box",
                    actions = {
                        IconButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://t.me/advanced_tecnology_official")
                                )
                                runCatching { context.startActivity(intent) }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_telegram),
                                contentDescription = "Telegram channel"
                            )
                        }
                        IconButton(onClick = { onEvent(MainUiEvent.ToolbarAddClicked) }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Create section"
                            )
                        }
                        Box {
                            IconButton(onClick = { overflowExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = overflowExpanded,
                                onDismissRequest = { overflowExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Manage Sections") },
                                    onClick = {
                                        overflowExpanded = false
                                        onEvent(MainUiEvent.OpenManageSections)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort Apps") },
                                    onClick = {
                                        overflowExpanded = false
                                        onEvent(MainUiEvent.ShowSortDialog)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Refresh Installed Apps") },
                                    onClick = {
                                        overflowExpanded = false
                                        onEvent(MainUiEvent.RefreshInstalledApps)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        overflowExpanded = false
                                        onEvent(MainUiEvent.OpenSettings)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("About") },
                                    onClick = {
                                        overflowExpanded = false
                                        onEvent(MainUiEvent.ShowAboutDialog)
                                    }
                                )
                            }
                        }
                    }
                )
                if (uiState.sections.isNotEmpty()) {
                    SectionTabRow(
                        sections = uiState.sections,
                        selectedSectionId = uiState.selectedSectionId,
                        onSectionSelected = { onEvent(MainUiEvent.SectionSelected(it)) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.selectedSection != null) {
                LauncherFab(
                    onClick = { onEvent(MainUiEvent.FloatingAddClicked) },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(sections, selectedSectionId) {
                    if (sections.size < 2 || selectedSectionId == null) return@pointerInput
                    var dragTotal = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { dragTotal = 0f },
                        onDragEnd = {
                            val index = sections.indexOfFirst { it.id == selectedSectionId }
                            val target = when {
                                dragTotal <= -swipeThreshold -> sections.getOrNull(index + 1)
                                dragTotal >= swipeThreshold -> sections.getOrNull(index - 1)
                                else -> null
                            }
                            if (target != null) onEvent(MainUiEvent.SectionSelected(target.id))
                            dragTotal = 0f
                        },
                        onDragCancel = { dragTotal = 0f }
                    ) { _, dragAmount -> dragTotal += dragAmount }
                }
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )

                !uiState.hasAnySection -> EmptySectionState(
                    title = "No sections yet",
                    message = "Create a section to start organizing your apps",
                    action = {
                        TextButton(onClick = { onEvent(MainUiEvent.ToolbarAddClicked) }) {
                            Text("Create section", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )

                !uiState.hasVisibleSections -> EmptySectionState(
                    title = "All sections are hidden",
                    message = "Show a section from Manage Sections to see your apps",
                    action = {
                        TextButton(onClick = { onEvent(MainUiEvent.OpenManageSections) }) {
                            Text("Manage sections", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )

                uiState.apps.isEmpty() -> EmptySectionState(
                    title = "No apps added yet",
                    message = "Tap + to add apps to this section"
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = remember(selectedSectionId) { LazyListState() },
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    item(key = "section-header") {
                        SectionHeader(title = uiState.selectedSection?.name.orEmpty())
                    }
                    items(items = uiState.apps, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app,
                            subtitle = if (app.isInstalled) app.packageName else "Not installed",
                            showSubtitle = uiState.showSubtitles,
                            onClick = { onEvent(MainUiEvent.AppClicked(app.packageName)) },
                            onLongClick = { onEvent(MainUiEvent.AppLongPressed(app.packageName)) },
                            onMenuClick = { onEvent(MainUiEvent.AppLongPressed(app.packageName)) }
                        )
                    }
                }
            }
        }
    }

    val dialogState = uiState.dialog

    if (dialogState.showSelectSectionDialog) {
        SelectSectionDialog(
            onTypeSelected = { onEvent(MainUiEvent.SectionTypeSelected(it)) },
            onDismiss = { onEvent(MainUiEvent.DismissSelectSectionDialog) }
        )
    }

    dialogState.sectionDialog?.let { form ->
        SectionFormDialog(
            state = form,
            onNameChanged = { onEvent(MainUiEvent.SectionNameChanged(it)) },
            onColorSelected = { onEvent(MainUiEvent.SectionColorChanged(it)) },
            onDefaultChanged = {},
            onVisibleChanged = {},
            onDelete = null,
            onConfirm = { onEvent(MainUiEvent.ConfirmSectionDialog) },
            onDismiss = { onEvent(MainUiEvent.DismissSectionDialog) }
        )
    }

    dialogState.appMenu?.let { menu ->
        AppContextMenu(
            menu = menu,
            onOpen = { onEvent(MainUiEvent.AppClicked(menu.packageName)) },
            onRemove = { onEvent(MainUiEvent.RemoveAppRequested(menu.packageName)) },
            onAddToAnother = { onEvent(MainUiEvent.AddToAnotherSectionRequested(menu.packageName)) },
            onMoveToAnother = { onEvent(MainUiEvent.MoveToAnotherSectionRequested(menu.packageName)) },
            onAppInfo = { onEvent(MainUiEvent.AppInfoRequested(menu.packageName)) },
            onDismiss = { onEvent(MainUiEvent.DismissAppMenu) }
        )
    }

    dialogState.sectionPicker?.let { picker ->
        SectionPickerDialog(
            mode = picker.mode,
            sections = uiState.sections.filter { it.id != uiState.selectedSectionId },
            onPick = { onEvent(MainUiEvent.PickSection(it)) },
            onDismiss = { onEvent(MainUiEvent.DismissSectionPicker) }
        )
    }

    dialogState.pendingRemoval?.let { pending ->
        ConfirmRemovalDialog(
            label = pending.label,
            onConfirm = { onEvent(MainUiEvent.ConfirmRemoval) },
            onDismiss = { onEvent(MainUiEvent.DismissRemoval) }
        )
    }

    if (dialogState.showSortDialog) {
        SortAppsDialog(
            current = uiState.sortMode,
            onSelect = { onEvent(MainUiEvent.SortModeSelected(it)) },
            onDismiss = { onEvent(MainUiEvent.DismissSortDialog) }
        )
    }

    if (dialogState.showAboutDialog) {
        AboutDialog(onDismiss = { onEvent(MainUiEvent.DismissAboutDialog) })
    }
}
