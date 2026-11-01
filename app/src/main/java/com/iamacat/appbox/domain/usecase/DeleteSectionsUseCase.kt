package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository

/**
 * Removes several sections and their memberships in one pass. Installed
 * applications are never touched.
 */
class DeleteSectionsUseCase(
    private val sectionRepository: SectionRepository,
    private val membershipRepository: MembershipRepository
) {

    suspend operator fun invoke(sectionIds: Collection<String>) {
        if (sectionIds.isEmpty()) return
        sectionIds.forEach { membershipRepository.removeAllForSection(it) }
        sectionRepository.deleteSections(sectionIds)
    }
}
