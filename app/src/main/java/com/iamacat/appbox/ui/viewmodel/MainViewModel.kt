package com.iamacat.appbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.AppSettings
import com.iamacat.appbox.data.model.SectionType
import com.iamacat.appbox.data.repository.AppRepository
import com.iamacat.appbox.data.repository.InstalledAppsRepository
import com.iamacat.appbox.data.repository.SettingsRepository
import com.iamacat.appbox.domain.usecase.AddAppsResult
import com.iamacat.appbox.domain.usecase.AddAppsToSectionUseCase
import com.iamacat.appbox.domain.usecase.CreateSectionResult
import com.iamacat.appbox.domain.usecase.CreateSectionUseCase
import com.iamacat.appbox.domain.usecase.MoveAppResult
import com.iamacat.appbox.domain.usecase.MoveAppToSectionUseCase
import com.iamacat.appbox.domain.usecase.NewSection
import com.iamacat.appbox.domain.usecase.ObserveSectionsUseCase
import com.iamacat.appbox.domain.usecase.RemoveAppFromSectionUseCase
import com.iamacat.appbox.domain.usecase.SectionNameValidator
import com.iamacat.appbox.domain.usecase.SortAppsUseCase
import com.iamacat.appbox.ui.event.MainEffect
import com.iamacat.appbox.ui.event.MainUiEvent
import com.iamacat.appbox.ui.state.AppMenuState
import com.iamacat.appbox.ui.state.MainDialogState
import com.iamacat.appbox.ui.state.MainUiState
import com.iamacat.appbox.ui.state.PendingRemoval
import com.iamacat.appbox.ui.state.PickSectionMode
import com.iamacat.appbox.ui.state.SectionDialogMode
import com.iamacat.appbox.ui.state.SectionDialogUiState
import com.iamacat.appbox.ui.state.SectionPickerState
import com.iamacat.appbox.util.AppLauncher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val appRepository: AppRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val settingsRepository: SettingsRepository,
    private val appLauncher: AppLauncher,
    observeSections: ObserveSectionsUseCase,
    private val createSection: CreateSectionUseCase,
    private val sortApps: SortAppsUseCase,
    private val addAppsToSection: AddAppsToSectionUseCase,
    private val removeApp: RemoveAppFromSectionUseCase,
    private val moveApp: MoveAppToSectionUseCase
) : ViewModel() {

    private val allSections = MutableStateFlow<List<AppSection>?>(null)
    private val selectedId = MutableStateFlow<String?>(null)
    private val sectionApps = MutableStateFlow<List<AppEntry>?>(null)
    private val settings = MutableStateFlow(AppSettings())
    private val dialog = MutableStateFlow(MainDialogState())

    private val _effects = Channel<MainEffect>(Channel.BUFFERED)
    val effects: Flow<MainEffect> = _effects.receiveAsFlow()

    private var reconcileJob: Job? = null

    val uiState: StateFlow<MainUiState> = combine(
        allSections,
        selectedId,
        sectionApps,
        settings,
        dialog
    ) { sections, selected, entries, appSettings, dialogState ->
        val all = sections.orEmpty()
        val visible = all.filter { it.isVisible }
        val selectedSection = visible.firstOrNull { it.id == selected }
        MainUiState(
            sections = visible,
            selectedSectionId = selectedSection?.id,
            selectedSection = selectedSection,
            apps = sortApps(entries.orEmpty(), appSettings.sortMode),
            showSubtitles = appSettings.showSubtitles,
            sortMode = appSettings.sortMode,
            isLoading = sections == null || (selectedSection != null && entries == null),
            hasAnySection = all.isNotEmpty(),
            dialog = dialogState
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        viewModelScope.launch {
            observeSections().collect { sections ->
                allSections.value = sections
                reconcileSelection(sections)
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { latest -> settings.value = latest }
        }
        viewModelScope.launch {
            selectedId.flatMapLatest { id ->
                if (id == null) flowOf(emptyList()) else appRepository.observeAppsForSection(id)
            }.collect { apps -> sectionApps.value = apps }
        }
        refreshInstalledApps(announce = false)
    }

    fun onEvent(event: MainUiEvent) {
        when (event) {
            MainUiEvent.ToolbarAddClicked ->
                updateDialog { it.copy(showSelectSectionDialog = true) }

            MainUiEvent.DismissSelectSectionDialog ->
                updateDialog { it.copy(showSelectSectionDialog = false) }

            is MainUiEvent.SectionTypeSelected -> startCreateSectionFor(event.type)

            is MainUiEvent.SectionSelected -> setSelection(event.sectionId)

            is MainUiEvent.AppClicked -> openApp(event.packageName)

            is MainUiEvent.AppLongPressed -> {
                val app = findApp(event.packageName)
                updateDialog {
                    it.copy(
                        appMenu = AppMenuState(
                            packageName = event.packageName,
                            label = app?.label ?: event.packageName,
                            isInstalled = app?.isInstalled ?: true
                        )
                    )
                }
            }

            MainUiEvent.DismissAppMenu -> updateDialog { it.copy(appMenu = null) }

            is MainUiEvent.RemoveAppRequested -> requestRemoval(event.packageName)

            is MainUiEvent.AddToAnotherSectionRequested ->
                openSectionPicker(event.packageName, PickSectionMode.ADD)

            is MainUiEvent.MoveToAnotherSectionRequested ->
                openSectionPicker(event.packageName, PickSectionMode.MOVE)

            is MainUiEvent.AppInfoRequested -> openAppInfo(event.packageName)

            MainUiEvent.DismissSectionPicker -> updateDialog { it.copy(sectionPicker = null) }

            is MainUiEvent.PickSection -> pickSection(event.sectionId)

            is MainUiEvent.SectionNameChanged ->
                updateSectionDialog { it.copy(sectionName = event.name, validationError = null) }

            is MainUiEvent.SectionColorChanged ->
                updateSectionDialog { it.copy(selectedColorArgb = event.colorArgb) }

            MainUiEvent.ConfirmSectionDialog -> createSectionFromDialog()

            MainUiEvent.DismissSectionDialog -> updateDialog { it.copy(sectionDialog = null) }

            MainUiEvent.ConfirmRemoval ->
                dialog.value.pendingRemoval?.let { performRemoval(it.packageName) }

            MainUiEvent.DismissRemoval -> updateDialog { it.copy(pendingRemoval = null) }

            MainUiEvent.ShowSortDialog -> updateDialog { it.copy(showSortDialog = true) }

            MainUiEvent.DismissSortDialog -> updateDialog { it.copy(showSortDialog = false) }

            is MainUiEvent.SortModeSelected -> {
                updateDialog { it.copy(showSortDialog = false) }
                viewModelScope.launch { settingsRepository.setSortMode(event.sortMode) }
            }

            MainUiEvent.ShowAboutDialog -> updateDialog { it.copy(showAboutDialog = true) }

            MainUiEvent.DismissAboutDialog -> updateDialog { it.copy(showAboutDialog = false) }

            MainUiEvent.RefreshInstalledApps -> refreshInstalledApps(announce = true)

            MainUiEvent.OpenManageSections ->
                viewModelScope.launch { _effects.send(MainEffect.NavigateToManageSections) }

            MainUiEvent.OpenSettings ->
                viewModelScope.launch { _effects.send(MainEffect.NavigateToSettings) }

            MainUiEvent.ClearError -> updateDialog { it.copy(errorMessage = null) }

            MainUiEvent.FloatingAddClicked -> {
                val id = selectedId.value
                viewModelScope.launch {
                    if (id == null) {
                        _effects.send(MainEffect.ShowMessage("Create a section first"))
                    } else {
                        _effects.send(MainEffect.NavigateToAddApps(id))
                    }
                }
            }
        }
    }

    private fun updateDialog(block: (MainDialogState) -> MainDialogState) {
        dialog.update(block)
    }

    private fun updateSectionDialog(block: (SectionDialogUiState) -> SectionDialogUiState) {
        dialog.update { state ->
            state.copy(sectionDialog = state.sectionDialog?.let(block))
        }
    }

    private fun reconcileSelection(sections: List<AppSection>) {
        val visible = sections.filter { it.isVisible }
        val current = selectedId.value
        if (current != null && visible.any { it.id == current }) return

        reconcileJob?.cancel()
        reconcileJob = viewModelScope.launch {
            val preferred = runCatching { settingsRepository.current().lastSectionId }.getOrNull()
            val next = visible.firstOrNull { it.id == preferred }?.id
                ?: visible.firstOrNull { it.isDefault }?.id
                ?: visible.firstOrNull()?.id
            applySelection(next)
        }
    }

    private fun setSelection(sectionId: String) {
        applySelection(sectionId)
    }

    private fun applySelection(sectionId: String?) {
        selectedId.value = sectionId
        if (sectionId != null) {
            viewModelScope.launch {
                runCatching { settingsRepository.setLastSectionId(sectionId) }
            }
        }
    }

    private fun startCreateSectionFor(type: SectionType) {
        val existingNames = allSections.value.orEmpty().map { it.name }
        val validation = SectionNameValidator.validate(type.label, existingNames)
        updateDialog { state ->
            state.copy(
                showSelectSectionDialog = false,
                sectionDialog = SectionDialogUiState(
                    mode = SectionDialogMode.CREATE,
                    sectionName = type.label,
                    selectedType = type,
                    selectedColorArgb = type.defaultColorArgb,
                    validationError = SectionNameValidator.errorMessage(validation)
                )
            )
        }
    }

    private fun createSectionFromDialog() {
        val form = dialog.value.sectionDialog ?: return
        if (form.isSaving) return
        updateSectionDialog { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = createSection(NewSection(form.sectionName, form.selectedType, form.selectedColorArgb))) {
                is CreateSectionResult.Success -> {
                    updateDialog { it.copy(sectionDialog = null) }
                    applySelection(result.section.id)
                    _effects.send(MainEffect.ShowMessage("Section \"${result.section.name}\" created"))
                }

                is CreateSectionResult.Invalid ->
                    updateSectionDialog { it.copy(isSaving = false, validationError = result.message) }
            }
        }
    }

    private fun requestRemoval(packageName: String) {
        val app = findApp(packageName)
        if (settings.value.confirmBeforeRemove) {
            updateDialog {
                it.copy(
                    appMenu = null,
                    pendingRemoval = PendingRemoval(packageName, app?.label ?: packageName)
                )
            }
        } else {
            performRemoval(packageName)
        }
    }

    private fun performRemoval(packageName: String) {
        val sectionId = selectedId.value ?: return
        val label = findApp(packageName)?.label ?: packageName
        updateDialog { it.copy(appMenu = null, pendingRemoval = null) }
        viewModelScope.launch {
            removeApp(sectionId, packageName)
            _effects.send(MainEffect.ShowMessage("\"$label\" removed from this section"))
        }
    }

    private fun openSectionPicker(packageName: String, mode: PickSectionMode) {
        val app = findApp(packageName)
        updateDialog {
            it.copy(
                appMenu = null,
                sectionPicker = SectionPickerState(
                    packageName = packageName,
                    label = app?.label ?: packageName,
                    mode = mode
                )
            )
        }
    }

    private fun pickSection(sectionId: String) {
        val picker = dialog.value.sectionPicker ?: return
        val current = selectedId.value
        val target = allSections.value.orEmpty().firstOrNull { it.id == sectionId } ?: return
        updateDialog { it.copy(sectionPicker = null) }

        viewModelScope.launch {
            when (picker.mode) {
                PickSectionMode.ADD -> when (val result = addAppsToSection(sectionId, listOf(picker.packageName))) {
                    is AddAppsResult.Success -> _effects.send(
                        MainEffect.ShowMessage(
                            if (result.addedCount > 0) {
                                "\"${picker.label}\" added to ${target.name}"
                            } else {
                                "\"${picker.label}\" is already in ${target.name}"
                            }
                        )
                    )

                    AddAppsResult.SectionMissing ->
                        _effects.send(MainEffect.ShowMessage("That section no longer exists"))
                }

                PickSectionMode.MOVE -> {
                    if (current == null || current == sectionId) return@launch
                    when (val result = moveApp(current, sectionId, picker.packageName)) {
                        MoveAppResult.Success -> _effects.send(
                            MainEffect.ShowMessage("\"${picker.label}\" moved to ${target.name}")
                        )

                        MoveAppResult.NotInSection -> _effects.send(
                            MainEffect.ShowMessage("\"${picker.label}\" is no longer in this section")
                        )

                        MoveAppResult.SectionMissing -> _effects.send(
                            MainEffect.ShowMessage("That section no longer exists")
                        )
                    }
                }
            }
        }
    }

    private fun openApp(packageName: String) {
        updateDialog { it.copy(appMenu = null) }
        val label = findApp(packageName)?.label ?: packageName
        if (!appLauncher.openApp(packageName)) {
            viewModelScope.launch {
                _effects.send(MainEffect.ShowMessage("\"$label\" is not installed"))
            }
        }
    }

    private fun openAppInfo(packageName: String) {
        updateDialog { it.copy(appMenu = null) }
        if (!appLauncher.openAppInfo(packageName)) {
            viewModelScope.launch {
                _effects.send(MainEffect.ShowMessage("Could not open app info"))
            }
        }
    }

    private fun refreshInstalledApps(announce: Boolean) {
        viewModelScope.launch {
            val result = runCatching { installedAppsRepository.refresh() }
            if (announce) {
                result.onSuccess {
                    _effects.send(MainEffect.ShowMessage("Installed apps refreshed"))
                }.onFailure {
                    _effects.send(MainEffect.ShowMessage("Could not refresh apps"))
                }
            }
        }
    }

    private fun findApp(packageName: String): AppEntry? =
        sectionApps.value?.firstOrNull { it.packageName == packageName }
            ?: installedAppsRepository.installedApps.value.firstOrNull { it.packageName == packageName }
}
