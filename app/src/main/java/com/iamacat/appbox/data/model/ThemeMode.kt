package com.iamacat.appbox.data.model

enum class ThemeMode(val key: String, val label: String) {
    LIGHT("light", "Light"),
    SYSTEM("system", "System default");

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}
