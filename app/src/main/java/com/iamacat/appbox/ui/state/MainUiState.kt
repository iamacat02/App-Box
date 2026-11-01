package com.iamacat.appbox.ui.state

import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SortMode

enum class PickSectionMode { ADD, MOVE }

data class AppMenuState(
    val packageName: String,
    val label: String,
    val isInstalled: Boolean
)

data class SectionPickerState(
    val packageName: String,
    val label: String,
    val mode: PickSectionMode
)

data class PendingRemoval(
    val packageName: String,
    val label: String
)

data class MainDialogState(
    val showSelectSectionDialog: Boolean = false,
    val sectionDialog: SectionDialogUiState? = null,
    val appMenu: AppMenuState? = null,
    val sectionPicker: SectionPickerState? = null,
    val pendingRemoval: PendingRemoval? = null,
    val showSortDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val errorMessage: String? = null
)

data class MainUiState(
    val sections: List<AppSection> = emptyList(),
    val selectedSectionId: String? = null,
    val selectedSection: AppSection? = null,
    val apps: List<AppEntry> = emptyList(),
    val showSubtitles: Boolean = true,
    val sortMode: SortMode = SortMode.NAME_ASC,
    val isLoading: Boolean = true,
    val hasAnySection: Boolean = false,
    val dialog: MainDialogState = MainDialogState()
) {
    val hasVisibleSections: Boolean get() = sections.isNotEmpty()
}
