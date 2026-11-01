package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository

/**
 * Removes the section record and its memberships. Installed applications are
 * never touched.
 */
class DeleteSectionUseCase(
    private val sectionRepository: SectionRepository,
    private val membershipRepository: MembershipRepository
) {

    suspend operator fun invoke(sectionId: String) {
        membershipRepository.removeAllForSection(sectionId)
        sectionRepository.deleteSection(sectionId)
    }
}
