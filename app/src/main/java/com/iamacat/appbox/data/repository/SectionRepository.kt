package com.iamacat.appbox.data.repository

import com.iamacat.appbox.data.local.SectionDao
import com.iamacat.appbox.data.mapper.SectionMapper
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SectionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SectionRepository(private val sectionDao: SectionDao) {

    fun observeSections(): Flow<List<AppSection>> =
        sectionDao.observeAll().map { entities -> entities.map(SectionMapper::toDomain) }

    suspend fun getSections(): List<AppSection> =
        sectionDao.getAll().map(SectionMapper::toDomain)

    suspend fun getSection(id: String): AppSection? =
        sectionDao.getById(id)?.let(SectionMapper::toDomain)

    suspend fun createSection(
        name: String,
        type: SectionType,
        colorArgb: Long,
        isDefault: Boolean = false
    ): AppSection {
        val order = sectionDao.maxOrder() + 1
        val section = AppSection(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            colorArgb = colorArgb,
            order = order,
            isVisible = true,
            isDefault = false
        )
        sectionDao.insert(SectionMapper.toEntity(section))
        if (isDefault) {
            sectionDao.setDefault(section.id)
            return section.copy(isDefault = true)
        }
        return section
    }

    suspend fun updateSection(section: AppSection) {
        sectionDao.update(SectionMapper.toEntity(section))
        if (section.isDefault) {
            sectionDao.setDefault(section.id)
        }
    }

    suspend fun deleteSection(id: String) {
        sectionDao.deleteById(id)
    }

    suspend fun deleteSections(ids: Collection<String>) {
        if (ids.isEmpty()) return
        sectionDao.deleteByIds(ids.toList())
    }

    suspend fun setDefault(id: String) {
        sectionDao.setDefault(id)
    }

    suspend fun clearDefault() {
        sectionDao.clearDefaultFlags()
    }

    suspend fun setVisible(id: String, isVisible: Boolean) {
        val section = getSection(id) ?: return
        if (section.isVisible == isVisible) return
        sectionDao.update(SectionMapper.toEntity(section.copy(isVisible = isVisible)))
    }

    suspend fun reorderSections(orderedIds: List<String>) {
        sectionDao.replaceOrder(orderedIds)
    }

    suspend fun nameExists(name: String, excludeId: String? = null): Boolean {
        val candidate = name.trim()
        if (candidate.isEmpty()) return false
        return sectionDao.getAll().any {
            it.id != excludeId && it.name.trim().equals(candidate, ignoreCase = true)
        }
    }
}
