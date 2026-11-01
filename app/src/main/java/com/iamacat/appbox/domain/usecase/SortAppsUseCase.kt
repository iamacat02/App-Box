package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.data.model.SortMode

class SortAppsUseCase {

    operator fun invoke(apps: List<AppEntry>, sortMode: SortMode): List<AppEntry> = when (sortMode) {
        SortMode.NAME_ASC -> apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        SortMode.NAME_DESC -> apps.sortedWith(
            compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.label }
        )
        SortMode.CUSTOM -> apps
    }
}
