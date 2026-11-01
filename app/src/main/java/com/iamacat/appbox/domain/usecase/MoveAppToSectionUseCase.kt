package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository

sealed interface MoveAppResult {
    data object Success : MoveAppResult
    data object SectionMissing : MoveAppResult
    data object NotInSection : MoveAppResult
}

class MoveAppToSectionUseCase(
    private val sectionRepository: SectionRepository,
    private val membershipRepository: MembershipRepository
) {

    suspend operator fun invoke(fromSectionId: String, toSectionId: String, packageName: String): MoveAppResult {
        if (sectionRepository.getSection(toSectionId) == null) return MoveAppResult.SectionMissing
        val moved = membershipRepository.movePackage(fromSectionId, toSectionId, packageName)
        return if (moved) MoveAppResult.Success else MoveAppResult.NotInSection
    }
}
