package com.iamacat.appbox.domain

import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.data.model.SortMode
import com.iamacat.appbox.domain.usecase.SortAppsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class SortAppsUseCaseTest {

    private val useCase = SortAppsUseCase()
    private val apps = listOf(
        AppEntry("org.zebra", "zebra"),
        AppEntry("com.alpha", "Alpha"),
        AppEntry("com.beta", "beta")
    )

    @Test
    fun `sorts alphabetically ascending ignoring case`() {
        val sorted = useCase(apps, SortMode.NAME_ASC)
        assertEquals(listOf("Alpha", "beta", "zebra"), sorted.map { it.label })
    }

    @Test
    fun `sorts alphabetically descending ignoring case`() {
        val sorted = useCase(apps, SortMode.NAME_DESC)
        assertEquals(listOf("zebra", "beta", "Alpha"), sorted.map { it.label })
    }

    @Test
    fun `custom order preserves membership order`() {
        val sorted = useCase(apps, SortMode.CUSTOM)
        assertEquals(listOf("zebra", "Alpha", "beta"), sorted.map { it.label })
    }
}
