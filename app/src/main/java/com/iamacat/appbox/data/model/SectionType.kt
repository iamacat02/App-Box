package com.iamacat.appbox.data.model

/**
 * Built-in section categories. [CUSTOM] is the fallback for user-defined names.
 */
enum class SectionType(
    val key: String,
    val label: String,
    val defaultColorArgb: Long
) {
    APPS("apps", "Apps", 0xFF159A24L),
    GAMES("games", "Games", 0xFF7B1FA2L),
    SOCIAL_MEDIA("social_media", "Social Media", 0xFF1976D2L),
    TOOLS("tools", "Tools", 0xFF455A64L),
    MEDIA("media", "Media", 0xFFE64A19L),
    CREATIVE("creative", "Creative", 0xFF00897BL),
    CUSTOM("custom", "Custom", 0xFF159A24L);

    companion object {
        fun fromKey(key: String?): SectionType =
            entries.firstOrNull { it.key == key } ?: CUSTOM
    }
}
