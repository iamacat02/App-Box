package com.iamacat.appbox.data.repository

import com.iamacat.appbox.data.local.MembershipDao
import com.iamacat.appbox.data.mapper.SectionMapper
import com.iamacat.appbox.data.model.SectionAppMembership
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MembershipRepository(private val membershipDao: MembershipDao) {

    fun observeMemberships(sectionId: String): Flow<List<SectionAppMembership>> =
        membershipDao.observeForSection(sectionId)
            .map { entities -> entities.map(SectionMapper::toDomain) }

    suspend fun addPackages(sectionId: String, packageNames: Collection<String>): Int =
        membershipDao.addMissing(sectionId, packageNames.toList())

    suspend fun removePackage(sectionId: String, packageName: String) {
        membershipDao.delete(sectionId, packageName)
    }

    suspend fun removeAllForSection(sectionId: String) {
        membershipDao.deleteForSection(sectionId)
    }

    suspend fun movePackage(fromSectionId: String, toSectionId: String, packageName: String): Boolean =
        membershipDao.moveTo(fromSectionId, toSectionId, packageName)

    suspend fun packagesIn(sectionId: String): Set<String> =
        membershipDao.packagesIn(sectionId).toHashSet()

    suspend fun sectionsContaining(packageName: String): Set<String> =
        membershipDao.sectionsContaining(packageName).toHashSet()
}
