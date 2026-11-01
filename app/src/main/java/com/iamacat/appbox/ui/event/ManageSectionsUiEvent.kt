package com.iamacat.appbox.ui.event

import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SectionType

sealed interface ManageSectionsUiEvent {
    data object AddSectionClicked : ManageSectionsUiEvent
    data object DismissTypeDialog : ManageSectionsUiEvent
    data class SectionTypeSelected(val type: SectionType) : ManageSectionsUiEvent

    data class EditSection(val section: AppSection) : ManageSectionsUiEvent
    data class SectionNameChanged(val name: String) : ManageSectionsUiEvent
    data class SectionColorChanged(val colorArgb: Long) : ManageSectionsUiEvent
    data class DialogDefaultChanged(val isDefault: Boolean) : ManageSectionsUiEvent
    data class DialogVisibilityChanged(val isVisible: Boolean) : ManageSectionsUiEvent
    data object SaveSection : ManageSectionsUiEvent
    data object DismissSectionDialog : ManageSectionsUiEvent

    data class RequestDelete(val section: AppSection) : ManageSectionsUiEvent
    data object DismissDelete : ManageSectionsUiEvent
    data object ConfirmDelete : ManageSectionsUiEvent

    data class StartSelection(val sectionId: String) : ManageSectionsUiEvent
    data class ToggleSelection(val sectionId: String) : ManageSectionsUiEvent
    data object ExitSelection : ManageSectionsUiEvent
    data object RequestBulkDelete : ManageSectionsUiEvent
    data object DismissBulkDelete : ManageSectionsUiEvent
    data object ConfirmBulkDelete : ManageSectionsUiEvent

    data class ToggleDefault(val sectionId: String) : ManageSectionsUiEvent
    data class ToggleVisibility(val sectionId: String) : ManageSectionsUiEvent
    data class MoveSection(val sectionId: String, val targetIndex: Int) : ManageSectionsUiEvent
    data object ClearError : ManageSectionsUiEvent
}

sealed interface ManageSectionsEffect {
    data class ShowMessage(val message: String) : ManageSectionsEffect
}
