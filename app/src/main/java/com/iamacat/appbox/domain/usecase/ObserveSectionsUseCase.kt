package com.iamacat.appbox.domain.usecase

import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.repository.SectionRepository
import kotlinx.coroutines.flow.Flow

class ObserveSectionsUseCase(private val sectionRepository: SectionRepository) {

    operator fun invoke(): Flow<List<AppSection>> = sectionRepository.observeSections()
}
