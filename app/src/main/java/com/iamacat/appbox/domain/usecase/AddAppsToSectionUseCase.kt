package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository

sealed interface AddAppsResult {
    data class Success(val addedCount: Int) : AddAppsResult
    data object SectionMissing : AddAppsResult
}

class AddAppsToSectionUseCase(
    private val sectionRepository: SectionRepository,
    private val membershipRepository: MembershipRepository
) {

    suspend operator fun invoke(sectionId: String, packageNames: Collection<String>): AddAppsResult {
        if (sectionRepository.getSection(sectionId) == null) return AddAppsResult.SectionMissing
        val added = membershipRepository.addPackages(sectionId, packageNames)
        return AddAppsResult.Success(added)
    }
}
