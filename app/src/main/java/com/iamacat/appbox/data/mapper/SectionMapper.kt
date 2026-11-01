package com.iamacat.appbox.data.mapper

import com.iamacat.appbox.data.local.AppMembershipEntity
import com.iamacat.appbox.data.local.AppSectionEntity
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SectionAppMembership
import com.iamacat.appbox.data.model.SectionType

object SectionMapper {

    fun toDomain(entity: AppSectionEntity): AppSection = AppSection(
        id = entity.id,
        name = entity.name,
        type = SectionType.fromKey(entity.type),
        colorArgb = entity.colorArgb,
        order = entity.order,
        isVisible = entity.isVisible,
        isDefault = entity.isDefault
    )

    fun toEntity(section: AppSection): AppSectionEntity = AppSectionEntity(
        id = section.id,
        name = section.name,
        type = section.type.key,
        colorArgb = section.colorArgb,
        order = section.order,
        isVisible = section.isVisible,
        isDefault = section.isDefault
    )

    fun toDomain(entity: AppMembershipEntity): SectionAppMembership = SectionAppMembership(
        sectionId = entity.sectionId,
        packageName = entity.packageName,
        order = entity.order
    )
}
