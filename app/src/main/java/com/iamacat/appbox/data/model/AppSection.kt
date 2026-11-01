package com.iamacat.appbox.data.model

data class AppSection(
    val id: String,
    val name: String,
    val type: SectionType,
    val colorArgb: Long,
    val order: Int,
    val isVisible: Boolean = true,
    val isDefault: Boolean = false
)
