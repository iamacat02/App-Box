package com.iamacat.appbox.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sections")
data class AppSectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    @ColumnInfo(name = "color_argb") val colorArgb: Long,
    @ColumnInfo(name = "sort_order") val order: Int,
    @ColumnInfo(name = "is_visible") val isVisible: Boolean,
    @ColumnInfo(name = "is_default") val isDefault: Boolean
)
