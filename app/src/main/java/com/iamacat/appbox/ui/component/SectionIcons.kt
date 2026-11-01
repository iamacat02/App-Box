package com.iamacat.appbox.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.iamacat.appbox.data.model.SectionType

object SectionIcons {
    fun iconFor(type: SectionType): ImageVector = when (type) {
        SectionType.APPS -> Icons.Outlined.Apps
        SectionType.GAMES -> Icons.Outlined.SportsEsports
        SectionType.SOCIAL_MEDIA -> Icons.Outlined.People
        SectionType.TOOLS -> Icons.Outlined.Build
        SectionType.MEDIA -> Icons.Outlined.Movie
        SectionType.CREATIVE -> Icons.Outlined.Palette
        SectionType.CUSTOM -> Icons.Outlined.Category
    }
}
