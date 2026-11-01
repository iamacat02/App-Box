package com.iamacat.appbox.fake

import com.iamacat.appbox.data.local.AppMembershipEntity
import com.iamacat.appbox.data.local.AppSectionEntity
import com.iamacat.appbox.data.local.MembershipDao
import com.iamacat.appbox.data.local.SectionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionDao(
    initial: List<AppSectionEntity> = emptyList()
) : SectionDao() {

    val rows = MutableStateFlow(initial)

    override fun observeAll(): Flow<List<AppSectionEntity>> =
        rows.map { list -> list.sortedBy { it.order } }

    override suspend fun getAll(): List<AppSectionEntity> = rows.value.sortedBy { it.order }

    override suspend fun getById(id: String): AppSectionEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun maxOrder(): Int = rows.value.maxOfOrNull { it.order } ?: -1

    override suspend fun insert(section: AppSectionEntity) {
        if (rows.value.any { it.id == section.id }) error("duplicate section id")
        rows.value = rows.value + section
    }

    override suspend fun update(section: AppSectionEntity) {
        rows.value = rows.value.map { if (it.id == section.id) section else it }
    }

    override suspend fun deleteById(id: String) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteByIds(ids: List<String>) {
        val targets = ids.toSet()
        rows.value = rows.value.filterNot { it.id in targets }
    }

    override suspend fun updateOrder(id: String, order: Int) {
        rows.value = rows.value.map { if (it.id == id) it.copy(order = order) else it }
    }

    override suspend fun clearDefaultFlags() {
        rows.value = rows.value.map { it.copy(isDefault = false) }
    }

    override suspend fun markDefault(id: String) {
        rows.value = rows.value.map { it.copy(isDefault = it.id == id) }
    }
}

class FakeMembershipDao(
    initial: List<AppMembershipEntity> = emptyList()
) : MembershipDao() {

    val rows = MutableStateFlow(initial)

    override fun observeForSection(sectionId: String): Flow<List<AppMembershipEntity>> =
        rows.map { list ->
            list.filter { it.sectionId == sectionId }.sortedBy { it.order }
        }

    override suspend fun getForSection(sectionId: String): List<AppMembershipEntity> =
        rows.value.filter { it.sectionId == sectionId }.sortedBy { it.order }

    override suspend fun packagesIn(sectionId: String): List<String> =
        rows.value.filter { it.sectionId == sectionId }.map { it.packageName }

    override suspend fun sectionsContaining(packageName: String): List<String> =
        rows.value.filter { it.packageName == packageName }.map { it.sectionId }

    override suspend fun maxOrder(sectionId: String): Int =
        rows.value.filter { it.sectionId == sectionId }.maxOfOrNull { it.order } ?: -1

    override suspend fun insertAll(members: List<AppMembershipEntity>) {
        val existing = rows.value.map { it.sectionId to it.packageName }.toHashSet()
        rows.value = rows.value + members.filterNot { (it.sectionId to it.packageName) in existing }
    }

    override suspend fun delete(sectionId: String, packageName: String) {
        rows.value = rows.value.filterNot {
            it.sectionId == sectionId && it.packageName == packageName
        }
    }

    override suspend fun deleteForSection(sectionId: String) {
        rows.value = rows.value.filterNot { it.sectionId == sectionId }
    }

    override suspend fun find(sectionId: String, packageName: String): AppMembershipEntity? =
        rows.value.firstOrNull { it.sectionId == sectionId && it.packageName == packageName }

    override suspend fun setPackageOrder(sectionId: String, packageName: String, order: Int) {
        rows.value = rows.value.map {
            if (it.sectionId == sectionId && it.packageName == packageName) {
                it.copy(order = order)
            } else {
                it
            }
        }
    }
}
