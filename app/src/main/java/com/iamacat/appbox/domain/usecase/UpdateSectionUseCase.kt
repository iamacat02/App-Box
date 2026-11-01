package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.repository.SectionRepository

sealed interface UpdateSectionResult {
    data object Success : UpdateSectionResult
    data class Invalid(val message: String) : UpdateSectionResult
}

class UpdateSectionUseCase(private val sectionRepository: SectionRepository) {

    suspend operator fun invoke(section: AppSection): UpdateSectionResult {
        val existingNames = sectionRepository.getSections()
            .filterNot { it.id == section.id }
            .map { it.name }
        val validation = SectionNameValidator.validate(section.name, existingNames)
        val error = SectionNameValidator.errorMessage(validation)
        if (error != null) return UpdateSectionResult.Invalid(error)

        sectionRepository.updateSection(
            section.copy(name = SectionNameValidator.normalize(section.name))
        )
        return UpdateSectionResult.Success
    }
}
