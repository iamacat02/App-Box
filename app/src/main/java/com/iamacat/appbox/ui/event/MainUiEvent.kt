package com.iamacat.appbox.ui.event

import com.iamacat.appbox.data.model.SectionType
import com.iamacat.appbox.data.model.SortMode

sealed interface MainUiEvent {
    data object ToolbarAddClicked : MainUiEvent
    data object FloatingAddClicked : MainUiEvent
    data object DismissSelectSectionDialog : MainUiEvent
    data class SectionTypeSelected(val type: SectionType) : MainUiEvent

    data class SectionSelected(val sectionId: String) : MainUiEvent
    data class AppClicked(val packageName: String) : MainUiEvent
    data class AppLongPressed(val packageName: String) : MainUiEvent
    data object DismissAppMenu : MainUiEvent
    data class RemoveAppRequested(val packageName: String) : MainUiEvent
    data class AddToAnotherSectionRequested(val packageName: String) : MainUiEvent
    data class MoveToAnotherSectionRequested(val packageName: String) : MainUiEvent
    data class AppInfoRequested(val packageName: String) : MainUiEvent
    data object DismissSectionPicker : MainUiEvent
    data class PickSection(val sectionId: String) : MainUiEvent

    data class SectionNameChanged(val name: String) : MainUiEvent
    data class SectionColorChanged(val colorArgb: Long) : MainUiEvent
    data object ConfirmSectionDialog : MainUiEvent
    data object DismissSectionDialog : MainUiEvent

    data object ConfirmRemoval : MainUiEvent
    data object DismissRemoval : MainUiEvent

    data object ShowSortDialog : MainUiEvent
    data object DismissSortDialog : MainUiEvent
    data class SortModeSelected(val sortMode: SortMode) : MainUiEvent
    data object ShowAboutDialog : MainUiEvent
    data object DismissAboutDialog : MainUiEvent

    data object RefreshInstalledApps : MainUiEvent
    data object OpenManageSections : MainUiEvent
    data object OpenSettings : MainUiEvent
    data object ClearError : MainUiEvent
}

sealed interface MainEffect {
    data class NavigateToAddApps(val sectionId: String) : MainEffect
    data object NavigateToManageSections : MainEffect
    data object NavigateToSettings : MainEffect
    data class ShowMessage(val message: String) : MainEffect
}
