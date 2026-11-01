package com.iamacat.appbox.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamacat.appbox.data.model.SortMode
import com.iamacat.appbox.data.model.ThemeMode
import com.iamacat.appbox.ui.component.LauncherToolbar
import com.iamacat.appbox.ui.component.SectionHeader
import com.iamacat.appbox.ui.component.SingleChoiceDialog
import com.iamacat.appbox.ui.component.SortAppsDialog
import com.iamacat.appbox.ui.event.SettingsEffect
import com.iamacat.appbox.ui.event.SettingsUiEvent
import com.iamacat.appbox.ui.state.SettingsUiState
import com.iamacat.appbox.ui.theme.GreenifyColors
import com.iamacat.appbox.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onBack: () -> Unit
) {
    var showDefaultSectionDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LauncherToolbar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item(key = "launcher-header") { SectionHeader("LAUNCHER") }
            item(key = "default-section") {
                SettingsValueRow(
                    title = "Default section",
                    value = uiState.defaultSectionName ?: "None",
                    onClick = { showDefaultSectionDialog = true }
                )
            }
            item(key = "sort") {
                SettingsValueRow(
                    title = "Sort apps",
                    value = uiState.settings.sortMode.label,
                    onClick = { showSortDialog = true }
                )
            }
            item(key = "subtitles") {
                SettingsSwitchRow(
                    title = "Show app subtitles",
                    checked = uiState.settings.showSubtitles,
                    onCheckedChange = { onEvent(SettingsUiEvent.ShowSubtitlesChanged(it)) }
                )
            }
            item(key = "behavior-header") { SectionHeader("BEHAVIOR") }
            item(key = "confirm-remove") {
                SettingsSwitchRow(
                    title = "Confirm before removing an app",
                    checked = uiState.settings.confirmBeforeRemove,
                    onCheckedChange = { onEvent(SettingsUiEvent.ConfirmBeforeRemoveChanged(it)) }
                )
            }
            item(key = "appearance-header") { SectionHeader("APPEARANCE") }
            item(key = "theme") {
                SettingsValueRow(
                    title = "Theme",
                    value = uiState.settings.themeMode.label,
                    onClick = { showThemeDialog = true }
                )
            }
            item(key = "restore") {
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
                    TextButton(onClick = { onEvent(SettingsUiEvent.RestoreDefaults) }) {
                        Text("Restore defaults", color = GreenifyColors.Primary)
                    }
                }
            }
        }
    }

    if (showDefaultSectionDialog) {
        val options = uiState.sections.map { it.name } + "None"
        val defaultIndex = uiState.defaultSectionId
            ?.let { id -> uiState.sections.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }
            ?: uiState.sections.size
        SingleChoiceDialog(
            title = "Default Section",
            options = options,
            selectedIndex = defaultIndex,
            onSelect = { index ->
                showDefaultSectionDialog = false
                val sectionId = uiState.sections.getOrNull(index)?.id
                onEvent(SettingsUiEvent.DefaultSectionSelected(sectionId))
            },
            onDismiss = { showDefaultSectionDialog = false }
        )
    }

    if (showSortDialog) {
        SortAppsDialog(
            current = uiState.settings.sortMode,
            onSelect = { onEvent(SettingsUiEvent.SortModeSelected(it)) },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showThemeDialog) {
        val modes = ThemeMode.entries
        SingleChoiceDialog(
            title = "Theme",
            options = modes.map { it.label },
            selectedIndex = modes.indexOf(uiState.settings.themeMode).coerceAtLeast(0),
            onSelect = { index ->
                showThemeDialog = false
                modes.getOrNull(index)?.let { onEvent(SettingsUiEvent.ThemeModeSelected(it)) }
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = 56.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 20.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .heightIn(min = 56.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = GreenifyColors.Primary)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 20.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
