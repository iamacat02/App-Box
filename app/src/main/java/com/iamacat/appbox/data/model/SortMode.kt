package com.iamacat.appbox.data.model

enum class SortMode(val key: String, val label: String) {
    NAME_ASC("name_asc", "Alphabetical A\u2013Z"),
    NAME_DESC("name_desc", "Alphabetical Z\u2013A"),
    CUSTOM("custom", "Custom order");

    companion object {
        fun fromKey(key: String?): SortMode =
            entries.firstOrNull { it.key == key } ?: NAME_ASC
    }
}
