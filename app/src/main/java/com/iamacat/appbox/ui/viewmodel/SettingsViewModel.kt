package com.iamacat.appbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamacat.appbox.data.repository.SectionRepository
import com.iamacat.appbox.data.repository.SettingsRepository
import com.iamacat.appbox.ui.event.SettingsEffect
import com.iamacat.appbox.ui.event.SettingsUiEvent
import com.iamacat.appbox.ui.state.SettingsUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val sectionRepository: SectionRepository
) : ViewModel() {

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        sectionRepository.observeSections()
    ) { settings, sections ->
        SettingsUiState(
            settings = settings,
            sections = sections,
            defaultSectionId = sections.firstOrNull { it.isDefault }?.id,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ShowSubtitlesChanged -> viewModelScope.launch {
                settingsRepository.setShowSubtitles(event.value)
            }

            is SettingsUiEvent.SortModeSelected -> viewModelScope.launch {
                settingsRepository.setSortMode(event.sortMode)
            }

            is SettingsUiEvent.ConfirmBeforeRemoveChanged -> viewModelScope.launch {
                settingsRepository.setConfirmBeforeRemove(event.value)
            }

            is SettingsUiEvent.ThemeModeSelected -> viewModelScope.launch {
                settingsRepository.setThemeMode(event.themeMode)
            }

            is SettingsUiEvent.DefaultSectionSelected -> viewModelScope.launch {
                if (event.sectionId == null) {
                    sectionRepository.clearDefault()
                } else {
                    sectionRepository.setDefault(event.sectionId)
                }
            }

            SettingsUiEvent.RestoreDefaults -> viewModelScope.launch {
                settingsRepository.restoreDefaults()
                _effects.send(SettingsEffect.ShowMessage("Settings restored to defaults"))
            }
        }
    }
}
