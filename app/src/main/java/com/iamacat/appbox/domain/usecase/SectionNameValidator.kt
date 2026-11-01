package com.iamacat.appbox.domain.usecase

sealed interface SectionNameResult {
    data object Valid : SectionNameResult
    data object Blank : SectionNameResult
    data object Duplicate : SectionNameResult
}

object SectionNameValidator {

    fun normalize(rawName: String): String = rawName.trim()

    fun validate(rawName: String, existingNames: Collection<String>): SectionNameResult {
        val candidate = normalize(rawName)
        if (candidate.isEmpty()) return SectionNameResult.Blank
        val duplicate = existingNames.any { it.trim().equals(candidate, ignoreCase = true) }
        return if (duplicate) SectionNameResult.Duplicate else SectionNameResult.Valid
    }

    fun errorMessage(result: SectionNameResult): String? = when (result) {
        SectionNameResult.Valid -> null
        SectionNameResult.Blank -> "Section name cannot be empty"
        SectionNameResult.Duplicate -> "A section with this name already exists"
    }
}
