package com.iamacat.appbox.data.model

data class AppSettings(
    val showSubtitles: Boolean = true,
    val sortMode: SortMode = SortMode.NAME_ASC,
    val confirmBeforeRemove: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastSectionId: String? = null
)
