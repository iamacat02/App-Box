package com.iamacat.appbox.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "memberships",
    primaryKeys = ["section_id", "package_name"],
    foreignKeys = [
        ForeignKey(
            entity = AppSectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["section_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("section_id")]
)
data class AppMembershipEntity(
    @ColumnInfo(name = "section_id") val sectionId: String,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "sort_order") val order: Int
)
