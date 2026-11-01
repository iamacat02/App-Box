package com.iamacat.appbox.ui.event

import com.iamacat.appbox.data.model.SortMode
import com.iamacat.appbox.data.model.ThemeMode

sealed interface SettingsUiEvent {
    data class ShowSubtitlesChanged(val value: Boolean) : SettingsUiEvent
    data class SortModeSelected(val sortMode: SortMode) : SettingsUiEvent
    data class ConfirmBeforeRemoveChanged(val value: Boolean) : SettingsUiEvent
    data class ThemeModeSelected(val themeMode: ThemeMode) : SettingsUiEvent
    data class DefaultSectionSelected(val sectionId: String?) : SettingsUiEvent
    data object RestoreDefaults : SettingsUiEvent
}

sealed interface SettingsEffect {
    data class ShowMessage(val message: String) : SettingsEffect
}
