package com.iamacat.appbox.domain

import com.iamacat.appbox.domain.usecase.SectionNameResult
import com.iamacat.appbox.domain.usecase.SectionNameValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SectionNameValidatorTest {

    private val existing = listOf("Apps", "Social Media", "GAMES")

    @Test
    fun `blank names are rejected`() {
        assertEquals(SectionNameResult.Blank, SectionNameValidator.validate("", existing))
        assertEquals(SectionNameResult.Blank, SectionNameValidator.validate("   ", existing))
    }

    @Test
    fun `names are unique ignoring case and surrounding whitespace`() {
        assertEquals(
            SectionNameResult.Duplicate,
            SectionNameValidator.validate("  social media ", existing)
        )
        assertEquals(
            SectionNameResult.Duplicate,
            SectionNameValidator.validate("games", existing)
        )
    }

    @Test
    fun `unique names are accepted`() {
        assertEquals(SectionNameResult.Valid, SectionNameValidator.validate("Creative", existing))
    }

    @Test
    fun `normalize trims surrounding whitespace`() {
        assertEquals("Creative", SectionNameValidator.normalize("  Creative  "))
    }

    @Test
    fun `error messages describe the failure`() {
        assertNotNull(SectionNameValidator.errorMessage(SectionNameResult.Blank))
        assertNotNull(SectionNameValidator.errorMessage(SectionNameResult.Duplicate))
        assertNull(SectionNameValidator.errorMessage(SectionNameResult.Valid))
    }
}
