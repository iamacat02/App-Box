package com.iamacat.appbox.data.repository

import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.data.local.MembershipDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Joins stored section memberships with the live installed-app inventory so
 * uninstalled packages can be reported instead of silently disappearing.
 */
class AppRepository(
    private val membershipDao: MembershipDao,
    private val installedAppsRepository: InstalledAppsRepository
) {

    fun observeAppsForSection(sectionId: String): Flow<List<AppEntry>> =
        combine(
            membershipDao.observeForSection(sectionId),
            installedAppsRepository.installedApps
        ) { memberships, installed ->
            val installedByPackage = installed.associateBy { it.packageName }
            memberships.map { membership ->
                installedByPackage[membership.packageName]
                    ?: AppEntry(
                        packageName = membership.packageName,
                        label = membership.packageName,
                        isInstalled = false
                    )
            }
        }
}
