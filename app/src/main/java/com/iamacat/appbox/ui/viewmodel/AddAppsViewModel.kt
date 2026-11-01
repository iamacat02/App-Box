package com.iamacat.appbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.data.repository.InstalledAppsRepository
import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository
import com.iamacat.appbox.domain.usecase.AddAppsResult
import com.iamacat.appbox.domain.usecase.AddAppsToSectionUseCase
import com.iamacat.appbox.ui.event.AddAppsEffect
import com.iamacat.appbox.ui.event.AddAppsUiEvent
import com.iamacat.appbox.ui.state.AddAppsUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAppsViewModel(
    private val sectionId: String,
    private val sectionRepository: SectionRepository,
    private val membershipRepository: MembershipRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val addAppsToSection: AddAppsToSectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAppsUiState(sectionId = sectionId))
    val uiState: StateFlow<AddAppsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AddAppsEffect>(Channel.BUFFERED)
    val effects: Flow<AddAppsEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            installedAppsRepository.installedApps.collect { apps ->
                _uiState.update { it.copy(installedApps = apps) }
            }
        }
        viewModelScope.launch {
            val section = runCatching { sectionRepository.getSection(sectionId) }.getOrNull()
            val existing = runCatching { membershipRepository.packagesIn(sectionId) }
                .getOrDefault(emptySet<String>())
            _uiState.update {
                it.copy(
                    sectionName = section?.name ?: it.sectionName,
                    existingPackageNames = existing,
                    errorMessage = if (section == null) "This section no longer exists" else it.errorMessage
                )
            }
            runCatching { installedAppsRepository.refresh() }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onEvent(event: AddAppsUiEvent) {
        when (event) {
            is AddAppsUiEvent.ToggleApp -> toggleApp(event.packageName)
            is AddAppsUiEvent.SearchQueryChanged ->
                _uiState.update { it.copy(searchQuery = event.query) }

            AddAppsUiEvent.Submit -> submit()
            AddAppsUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun toggleApp(packageName: String) {
        val state = _uiState.value
        if (packageName in state.existingPackageNames) return
        val updated = if (packageName in state.selectedPackageNames) {
            state.selectedPackageNames - packageName
        } else {
            state.selectedPackageNames + packageName
        }
        _uiState.update { it.copy(selectedPackageNames = updated) }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSaving || state.selectedPackageNames.isEmpty()) return
        if (state.errorMessage != null) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            when (val result = addAppsToSection(sectionId, state.selectedPackageNames)) {
                is AddAppsResult.Success -> {
                    _effects.send(AddAppsEffect.NavigateBack)
                    _effects.send(
                        AddAppsEffect.ShowMessage(
                            if (result.addedCount == 1) {
                                "1 app added to ${state.sectionName}"
                            } else {
                                "${result.addedCount} apps added to ${state.sectionName}"
                            }
                        )
                    )
                }

                AddAppsResult.SectionMissing -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = "This section no longer exists")
                }
            }
        }
    }
}
