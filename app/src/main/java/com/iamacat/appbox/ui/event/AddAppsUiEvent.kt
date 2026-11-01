package com.iamacat.appbox.ui.event

sealed interface AddAppsUiEvent {
    data class ToggleApp(val packageName: String) : AddAppsUiEvent
    data class SearchQueryChanged(val query: String) : AddAppsUiEvent
    data object Submit : AddAppsUiEvent
    data object ClearError : AddAppsUiEvent
}

sealed interface AddAppsEffect {
    data object NavigateBack : AddAppsEffect
    data class ShowMessage(val message: String) : AddAppsEffect
}
