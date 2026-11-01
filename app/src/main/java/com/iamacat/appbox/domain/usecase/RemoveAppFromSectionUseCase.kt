package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.repository.MembershipRepository

class RemoveAppFromSectionUseCase(private val membershipRepository: MembershipRepository) {

    suspend operator fun invoke(sectionId: String, packageName: String) {
        membershipRepository.removePackage(sectionId, packageName)
    }
}
