package com.iamacat.appbox.ui.state

import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.AppSettings

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val sections: List<AppSection> = emptyList(),
    val defaultSectionId: String? = null,
    val isLoading: Boolean = true
) {
    val defaultSectionName: String?
        get() = sections.firstOrNull { it.id == defaultSectionId }?.name
}
