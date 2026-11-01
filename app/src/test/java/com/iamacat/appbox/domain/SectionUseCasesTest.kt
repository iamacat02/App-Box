package com.iamacat.appbox.domain

import com.iamacat.appbox.data.local.AppSectionEntity
import com.iamacat.appbox.data.model.AppSection
import com.iamacat.appbox.data.model.SectionType
import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository
import com.iamacat.appbox.domain.usecase.AddAppsResult
import com.iamacat.appbox.domain.usecase.AddAppsToSectionUseCase
import com.iamacat.appbox.domain.usecase.CreateSectionResult
import com.iamacat.appbox.domain.usecase.CreateSectionUseCase
import com.iamacat.appbox.domain.usecase.DeleteSectionUseCase
import com.iamacat.appbox.domain.usecase.DeleteSectionsUseCase
import com.iamacat.appbox.domain.usecase.MoveAppResult
import com.iamacat.appbox.domain.usecase.MoveAppToSectionUseCase
import com.iamacat.appbox.domain.usecase.NewSection
import com.iamacat.appbox.domain.usecase.UpdateSectionResult
import com.iamacat.appbox.domain.usecase.UpdateSectionUseCase
import com.iamacat.appbox.fake.FakeMembershipDao
import com.iamacat.appbox.fake.FakeSectionDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionUseCasesTest {

    private fun sectionEntity(
        id: String,
        name: String,
        order: Int,
        type: SectionType = SectionType.CUSTOM
    ) = AppSectionEntity(
        id = id,
        name = name,
        type = type.key,
        colorArgb = 0xFF159A24L,
        order = order,
        isVisible = true,
        isDefault = false
    )

    @Test
    fun `create appends the section to the end of the order`() = runTest {
        val sectionDao = FakeSectionDao(
            listOf(
                sectionEntity("1", "Apps", 0),
                sectionEntity("2", "Games", 1)
            )
        )
        val repository = SectionRepository(sectionDao)
        val useCase = CreateSectionUseCase(repository)

        val result = useCase(NewSection("  Creative  ", SectionType.CREATIVE, 0xFF00897BL))

        assertTrue(result is CreateSectionResult.Success)
        val section = (result as CreateSectionResult.Success).section
        assertEquals("Creative", section.name)
        assertEquals(2, section.order)
        assertEquals(3, repository.getSections().size)
    }

    @Test
    fun `create rejects blank and duplicate names`() = runTest {
        val repository = SectionRepository(FakeSectionDao(listOf(sectionEntity("1", "Apps", 0))))
        val useCase = CreateSectionUseCase(repository)

        assertTrue(useCase(NewSection("   ", SectionType.CUSTOM, 0xFF159A24L)) is CreateSectionResult.Invalid)
        assertTrue(useCase(NewSection("apps", SectionType.CUSTOM, 0xFF159A24L)) is CreateSectionResult.Invalid)
        assertEquals(1, repository.getSections().size)
    }

    @Test
    fun `update rejects duplicate names but preserves the edited section id`() = runTest {
        val repository = SectionRepository(
            FakeSectionDao(
                listOf(
                    sectionEntity("1", "Apps", 0),
                    sectionEntity("2", "Games", 1)
                )
            )
        )
        val useCase = UpdateSectionUseCase(repository)
        val games = repository.getSections().first { it.id == "2" }

        val duplicate = useCase(games.copy(name = " APPS "))
        assertTrue(duplicate is UpdateSectionResult.Invalid)

        val renamed = useCase(games.copy(name = "  Play  "))
        assertTrue(renamed is UpdateSectionResult.Success)
        val stored = repository.getSections().first { it.id == "2" }
        assertEquals("Play", stored.name)
    }

    @Test
    fun `adding apps assigns sequential order and ignores duplicates`() = runTest {
        val sectionDao = FakeSectionDao(listOf(sectionEntity("1", "Apps", 0)))
        val membershipDao = FakeMembershipDao()
        val useCase = AddAppsToSectionUseCase(
            SectionRepository(sectionDao),
            MembershipRepository(membershipDao)
        )

        val first = useCase("1", listOf("com.b", "com.a", "com.b"))
        assertTrue(first is AddAppsResult.Success)
        assertEquals(2, (first as AddAppsResult.Success).addedCount)

        val second = useCase("1", listOf("com.a", "com.c"))
        assertEquals(1, (second as AddAppsResult.Success).addedCount)

        val stored = membershipDao.getForSection("1")
        assertEquals(listOf("com.b", "com.a", "com.c"), stored.map { it.packageName })
        assertEquals(listOf(0, 1, 2), stored.map { it.order })
    }

    @Test
    fun `an app can be added to more than one section`() = runTest {
        val sectionDao = FakeSectionDao(
            listOf(
                sectionEntity("1", "Apps", 0),
                sectionEntity("2", "Tools", 1)
            )
        )
        val membershipDao = FakeMembershipDao()
        val useCase = AddAppsToSectionUseCase(
            SectionRepository(sectionDao),
            MembershipRepository(membershipDao)
        )

        useCase("1", listOf("com.example.app"))
        useCase("2", listOf("com.example.app"))

        assertEquals(setOf("1", "2"), membershipDao.sectionsContaining("com.example.app").toSet())
    }

    @Test
    fun `adding apps to a missing section fails`() = runTest {
        val useCase = AddAppsToSectionUseCase(
            SectionRepository(FakeSectionDao(listOf(sectionEntity("1", "Apps", 0)))),
            MembershipRepository(FakeMembershipDao())
        )

        assertEquals(AddAppsResult.SectionMissing, useCase("missing", listOf("com.a")))
    }

    @Test
    fun `moving an app transfers the membership`() = runTest {
        val sectionDao = FakeSectionDao(
            listOf(
                sectionEntity("1", "Apps", 0),
                sectionEntity("2", "Tools", 1)
            )
        )
        val membershipDao = FakeMembershipDao()
        val membershipRepository = MembershipRepository(membershipDao)
        val sections = SectionRepository(sectionDao)

        AddAppsToSectionUseCase(sections, membershipRepository)("1", listOf("com.a", "com.b"))

        val useCase = MoveAppToSectionUseCase(sections, membershipRepository)
        assertEquals(MoveAppResult.Success, useCase("1", "2", "com.a"))
        assertEquals(listOf("com.b"), membershipDao.getForSection("1").map { it.packageName })
        assertEquals(listOf("com.a"), membershipDao.getForSection("2").map { it.packageName })
    }

    @Test
    fun `deleting a section removes memberships but never the app itself`() = runTest {
        val sectionDao = FakeSectionDao(listOf(sectionEntity("1", "Apps", 0)))
        val membershipDao = FakeMembershipDao()
        val membershipRepository = MembershipRepository(membershipDao)
        val sections = SectionRepository(sectionDao)
        AddAppsToSectionUseCase(sections, membershipRepository)("1", listOf("com.a"))

        DeleteSectionUseCase(sections, membershipRepository)("1")

        assertTrue(sections.getSections().isEmpty())
        assertTrue(membershipDao.rows.value.isEmpty())
    }

    @Test
    fun `deleting several sections removes their memberships and keeps the rest`() = runTest {
        val sectionDao = FakeSectionDao(
            listOf(
                sectionEntity("1", "Apps", 0),
                sectionEntity("2", "Games", 1),
                sectionEntity("3", "Tools", 2)
            )
        )
        val membershipDao = FakeMembershipDao()
        val membershipRepository = MembershipRepository(membershipDao)
        val sections = SectionRepository(sectionDao)
        val addApps = AddAppsToSectionUseCase(sections, membershipRepository)
        addApps("1", listOf("com.keep"))
        addApps("2", listOf("com.a", "com.b"))
        addApps("3", listOf("com.c"))

        DeleteSectionsUseCase(sections, membershipRepository)(listOf("2", "3"))

        assertEquals(listOf("1"), sections.getSections().map(AppSection::id))
        assertEquals(
            listOf("com.keep"),
            membershipDao.rows.value.map { it.packageName }
        )
    }

    @Test
    fun `setting a section as default clears the previous default`() = runTest {
        val sectionDao = FakeSectionDao(
            listOf(
                sectionEntity("1", "Apps", 0),
                sectionEntity("2", "Tools", 1)
            )
        )
        val repository = SectionRepository(sectionDao)

        repository.setDefault("1")
        assertEquals(listOf("1"), repository.getSections().filter { it.isDefault }.map { it.id })

        repository.setDefault("2")
        assertEquals(listOf("2"), repository.getSections().filter { it.isDefault }.map { it.id })
    }

    @Test
    fun `reorder persists the requested order`() = runTest {
        val sectionDao = FakeSectionDao(
            listOf(
                sectionEntity("1", "Apps", 0),
                sectionEntity("2", "Tools", 1),
                sectionEntity("3", "Games", 2)
            )
        )
        val repository = SectionRepository(sectionDao)

        repository.reorderSections(listOf("3", "1", "2"))

        assertEquals(
            listOf("3", "1", "2"),
            repository.getSections().map(AppSection::id)
        )
    }
}
