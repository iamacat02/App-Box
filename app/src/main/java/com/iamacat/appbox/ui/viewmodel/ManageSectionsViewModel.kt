package com.iamacat.appbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SectionType
import com.iamacat.appbox.data.repository.SectionRepository
import com.iamacat.appbox.domain.usecase.CreateSectionResult
import com.iamacat.appbox.domain.usecase.CreateSectionUseCase
import com.iamacat.appbox.domain.usecase.DeleteSectionUseCase
import com.iamacat.appbox.domain.usecase.DeleteSectionsUseCase
import com.iamacat.appbox.domain.usecase.NewSection
import com.iamacat.appbox.domain.usecase.ObserveSectionsUseCase
import com.iamacat.appbox.domain.usecase.SectionNameValidator
import com.iamacat.appbox.domain.usecase.UpdateSectionResult
import com.iamacat.appbox.domain.usecase.UpdateSectionUseCase
import com.iamacat.appbox.ui.event.ManageSectionsEffect
import com.iamacat.appbox.ui.event.ManageSectionsUiEvent
import com.iamacat.appbox.ui.state.ManageSectionsUiState
import com.iamacat.appbox.ui.state.SectionDialogMode
import com.iamacat.appbox.ui.state.SectionDialogUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageSectionsViewModel(
    private val sectionRepository: SectionRepository,
    observeSections: ObserveSectionsUseCase,
    private val createSection: CreateSectionUseCase,
    private val updateSection: UpdateSectionUseCase,
    private val deleteSection: DeleteSectionUseCase,
    private val deleteSections: DeleteSectionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageSectionsUiState())
    val uiState: StateFlow<ManageSectionsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ManageSectionsEffect>(Channel.BUFFERED)
    val effects: Flow<ManageSectionsEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeSections().collect { sections ->
                val ids = sections.map { it.id }.toSet()
                _uiState.update { state ->
                    val selected = state.selectedSectionIds intersect ids
                    state.copy(
                        sections = sections,
                        isLoading = false,
                        selectedSectionIds = selected,
                        selectionMode = state.selectionMode && selected.isNotEmpty(),
                        pendingDelete = state.pendingDelete?.takeIf { it.id in ids },
                        showBulkDeleteDialog = state.showBulkDeleteDialog && selected.isNotEmpty()
                    )
                }
            }
        }
    }

    fun onEvent(event: ManageSectionsUiEvent) {
        when (event) {
            ManageSectionsUiEvent.AddSectionClicked ->
                _uiState.update { it.copy(showSelectTypeDialog = true) }

            ManageSectionsUiEvent.DismissTypeDialog ->
                _uiState.update { it.copy(showSelectTypeDialog = false) }

            is ManageSectionsUiEvent.SectionTypeSelected -> openCreateFor(event.type)

            is ManageSectionsUiEvent.EditSection -> openEdit(event.section)

            is ManageSectionsUiEvent.SectionNameChanged ->
                updateDialog { it.copy(sectionName = event.name, validationError = null) }

            is ManageSectionsUiEvent.SectionColorChanged ->
                updateDialog { it.copy(selectedColorArgb = event.colorArgb) }

            is ManageSectionsUiEvent.DialogDefaultChanged ->
                updateDialog { it.copy(isDefault = event.isDefault) }

            is ManageSectionsUiEvent.DialogVisibilityChanged ->
                updateDialog { it.copy(isVisible = event.isVisible) }

            ManageSectionsUiEvent.SaveSection -> save()

            ManageSectionsUiEvent.DismissSectionDialog ->
                _uiState.update { it.copy(sectionDialog = null) }

            is ManageSectionsUiEvent.RequestDelete ->
                _uiState.update { it.copy(pendingDelete = event.section, sectionDialog = null) }

            ManageSectionsUiEvent.DismissDelete ->
                _uiState.update { it.copy(pendingDelete = null) }

            ManageSectionsUiEvent.ConfirmDelete -> confirmDelete()

            is ManageSectionsUiEvent.StartSelection ->
                _uiState.update {
                    it.copy(
                        selectionMode = true,
                        selectedSectionIds = it.selectedSectionIds + event.sectionId
                    )
                }

            is ManageSectionsUiEvent.ToggleSelection ->
                _uiState.update { state ->
                    val next = if (event.sectionId in state.selectedSectionIds) {
                        state.selectedSectionIds - event.sectionId
                    } else {
                        state.selectedSectionIds + event.sectionId
                    }
                    state.copy(selectionMode = next.isNotEmpty(), selectedSectionIds = next)
                }

            ManageSectionsUiEvent.ExitSelection ->
                _uiState.update { it.copy(selectionMode = false, selectedSectionIds = emptySet()) }

            ManageSectionsUiEvent.RequestBulkDelete ->
                _uiState.update {
                    if (it.selectedSectionIds.isEmpty()) it else it.copy(showBulkDeleteDialog = true)
                }

            ManageSectionsUiEvent.DismissBulkDelete ->
                _uiState.update { it.copy(showBulkDeleteDialog = false) }

            ManageSectionsUiEvent.ConfirmBulkDelete -> confirmBulkDelete()

            is ManageSectionsUiEvent.ToggleDefault -> {
                viewModelScope.launch { sectionRepository.setDefault(event.sectionId) }
            }

            is ManageSectionsUiEvent.ToggleVisibility -> {
                val section = _uiState.value.sections.firstOrNull { it.id == event.sectionId } ?: return
                viewModelScope.launch {
                    sectionRepository.setVisible(section.id, !section.isVisible)
                }
            }

            is ManageSectionsUiEvent.MoveSection ->
                moveSection(event.sectionId, event.targetIndex)

            ManageSectionsUiEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun updateDialog(block: (SectionDialogUiState) -> SectionDialogUiState) {
        _uiState.update { state ->
            state.copy(sectionDialog = state.sectionDialog?.let(block))
        }
    }

    private fun openCreateFor(type: SectionType) {
        val existingNames = _uiState.value.sections.map { it.name }
        val validation = SectionNameValidator.validate(type.label, existingNames)
        _uiState.update {
            it.copy(
                showSelectTypeDialog = false,
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

    private fun openEdit(section: AppSection) {
        _uiState.update {
            it.copy(
                sectionDialog = SectionDialogUiState(
                    mode = SectionDialogMode.EDIT,
                    sectionName = section.name,
                    selectedType = section.type,
                    selectedColorArgb = section.colorArgb,
                    isDefault = section.isDefault,
                    isVisible = section.isVisible,
                    editingSectionId = section.id
                )
            )
        }
    }

    private fun save() {
        val form = _uiState.value.sectionDialog ?: return
        if (form.isSaving) return
        _uiState.update { it.copy(sectionDialog = form.copy(isSaving = true)) }

        viewModelScope.launch {
            if (form.mode == SectionDialogMode.CREATE) {
                when (val result = createSection(
                    NewSection(form.sectionName, form.selectedType, form.selectedColorArgb, form.isDefault)
                )) {
                    is CreateSectionResult.Success -> {
                        _uiState.update { it.copy(sectionDialog = null) }
                        _effects.send(ManageSectionsEffect.ShowMessage("Section \"${result.section.name}\" created"))
                    }

                    is CreateSectionResult.Invalid ->
                        updateDialog { it.copy(isSaving = false, validationError = result.message) }
                }
            } else {
                val original = _uiState.value.sections.firstOrNull { it.id == form.editingSectionId }
                if (original == null) {
                    _uiState.update { it.copy(sectionDialog = null) }
                    _effects.send(ManageSectionsEffect.ShowMessage("That section no longer exists"))
                    return@launch
                }
                val updated = original.copy(
                    name = form.sectionName,
                    type = form.selectedType,
                    colorArgb = form.selectedColorArgb,
                    isDefault = form.isDefault,
                    isVisible = form.isVisible
                )
                when (val result = updateSection(updated)) {
                    UpdateSectionResult.Success -> {
                        _uiState.update { it.copy(sectionDialog = null) }
                        _effects.send(ManageSectionsEffect.ShowMessage("Section updated"))
                    }

                    is UpdateSectionResult.Invalid ->
                        updateDialog { it.copy(isSaving = false, validationError = result.message) }
                }
            }
        }
    }

    private fun confirmDelete() {
        val section = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            deleteSection(section.id)
            _effects.send(ManageSectionsEffect.ShowMessage("Section \"${section.name}\" deleted"))
        }
    }

    private fun confirmBulkDelete() {
        val ids = _uiState.value.selectedSectionIds
        _uiState.update { it.copy(showBulkDeleteDialog = false) }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            deleteSections(ids)
            _uiState.update { it.copy(selectionMode = false, selectedSectionIds = emptySet()) }
            val message = if (ids.size == 1) "1 section deleted" else "${ids.size} sections deleted"
            _effects.send(ManageSectionsEffect.ShowMessage(message))
        }
    }

    private fun moveSection(sectionId: String, targetIndex: Int) {
        val orderedIds = _uiState.value.sections.map { it.id }.toMutableList()
        val fromIndex = orderedIds.indexOf(sectionId)
        if (fromIndex < 0) return
        val toIndex = targetIndex.coerceIn(0, orderedIds.lastIndex)
        if (fromIndex == toIndex) return
        orderedIds.add(toIndex, orderedIds.removeAt(fromIndex))
        viewModelScope.launch {
            runCatching { sectionRepository.reorderSections(orderedIds) }
        }
    }
}
