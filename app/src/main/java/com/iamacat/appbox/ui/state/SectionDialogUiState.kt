package com.iamacat.appbox.ui.state

import com.iamacat.appbox.data.model.SectionType

enum class SectionDialogMode { CREATE, EDIT }

/**
 * Form state for the create/edit section dialogs. Kept separate from screen
 * state so the form can be reused by the main and manage-sections screens.
 */
data class SectionDialogUiState(
    val mode: SectionDialogMode = SectionDialogMode.CREATE,
    val sectionName: String = "",
    val selectedType: SectionType = SectionType.CUSTOM,
    val selectedColorArgb: Long = 0xFF159A24L,
    val isDefault: Boolean = false,
    val isVisible: Boolean = true,
    val editingSectionId: String? = null,
    val validationError: String? = null,
    val isSaving: Boolean = false
) {
    val isEditing: Boolean get() = mode == SectionDialogMode.EDIT
}
