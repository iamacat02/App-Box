package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SectionType
import com.iamacat.appbox.data.repository.SectionRepository

data class NewSection(
    val name: String,
    val type: SectionType,
    val colorArgb: Long,
    val isDefault: Boolean = false
)

sealed interface CreateSectionResult {
    data class Success(val section: AppSection) : CreateSectionResult
    data class Invalid(val message: String) : CreateSectionResult
}

class CreateSectionUseCase(private val sectionRepository: SectionRepository) {

    suspend operator fun invoke(draft: NewSection): CreateSectionResult {
        val existingNames = sectionRepository.getSections().map { it.name }
        val validation = SectionNameValidator.validate(draft.name, existingNames)
        val error = SectionNameValidator.errorMessage(validation)
        if (error != null) return CreateSectionResult.Invalid(error)

        return CreateSectionResult.Success(
            sectionRepository.createSection(
                name = SectionNameValidator.normalize(draft.name),
                type = draft.type,
                colorArgb = draft.colorArgb,
                isDefault = draft.isDefault
            )
        )
    }
}
