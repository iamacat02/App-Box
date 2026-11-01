package com.iamacat.appbox.data.model

data class AppEntry(
    val packageName: String,
    val label: String,
    val isInstalled: Boolean = true
)
