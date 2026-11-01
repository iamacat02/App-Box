package com.iamacat.appbox.ui.state

import com.iamacat.appbox.data.model.AppEntry

data class AddAppsUiState(
    val sectionId: String,
    val sectionName: String = "",
    val installedApps: List<AppEntry> = emptyList(),
    val existingPackageNames: Set<String> = emptySet(),
    val selectedPackageNames: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val searchActive: Boolean get() = searchQuery.isNotEmpty()

    val visibleApps: List<AppEntry> = if (searchQuery.isBlank()) {
        installedApps
    } else {
        val query = searchQuery.trim()
        installedApps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    val selectedCount: Int get() = selectedPackageNames.size
}
