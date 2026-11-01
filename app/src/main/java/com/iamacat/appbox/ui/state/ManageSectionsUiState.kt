package com.iamacat.appbox.ui.state

import com.iamacat.appbox.data.model.AppSection

data class ManageSectionsUiState(
    val sections: List<AppSection> = emptyList(),
    val isLoading: Boolean = true,
    val showSelectTypeDialog: Boolean = false,
    val sectionDialog: SectionDialogUiState? = null,
    val pendingDelete: AppSection? = null,
    val selectionMode: Boolean = false,
    val selectedSectionIds: Set<String> = emptySet(),
    val showBulkDeleteDialog: Boolean = false,
    val errorMessage: String? = null
)
