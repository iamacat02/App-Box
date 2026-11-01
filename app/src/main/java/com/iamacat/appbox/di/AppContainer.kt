package com.iamacat.appbox.di

import android.content.Context
import androidx.room.Room
import com.iamacat.appbox.data.local.AppDatabase
import com.iamacat.appbox.data.repository.AppRepository
import com.iamacat.appbox.data.repository.InstalledAppsRepository
import com.iamacat.appbox.data.repository.MembershipRepository
import com.iamacat.appbox.data.repository.SectionRepository
import com.iamacat.appbox.data.repository.SettingsRepository
import com.iamacat.appbox.domain.usecase.AddAppsToSectionUseCase
import com.iamacat.appbox.domain.usecase.CreateSectionUseCase
import com.iamacat.appbox.domain.usecase.DeleteSectionUseCase
import com.iamacat.appbox.domain.usecase.DeleteSectionsUseCase
import com.iamacat.appbox.domain.usecase.MoveAppToSectionUseCase
import com.iamacat.appbox.domain.usecase.ObserveSectionsUseCase
import com.iamacat.appbox.domain.usecase.RemoveAppFromSectionUseCase
import com.iamacat.appbox.domain.usecase.SortAppsUseCase
import com.iamacat.appbox.domain.usecase.UpdateSectionUseCase
import com.iamacat.appbox.util.AppLauncher

/**
 * Manual dependency injection graph, created once by the Application.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "appbox.db"
    ).build()

    val sectionRepository = SectionRepository(database.sectionDao())
    val membershipRepository = MembershipRepository(database.membershipDao())
    val installedAppsRepository = InstalledAppsRepository(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val appRepository = AppRepository(database.membershipDao(), installedAppsRepository)
    val appLauncher = AppLauncher(appContext)

    val observeSectionsUseCase = ObserveSectionsUseCase(sectionRepository)
    val createSectionUseCase = CreateSectionUseCase(sectionRepository)
    val updateSectionUseCase = UpdateSectionUseCase(sectionRepository)
    val deleteSectionUseCase = DeleteSectionUseCase(sectionRepository, membershipRepository)
    val deleteSectionsUseCase = DeleteSectionsUseCase(sectionRepository, membershipRepository)
    val addAppsToSectionUseCase = AddAppsToSectionUseCase(sectionRepository, membershipRepository)
    val removeAppFromSectionUseCase = RemoveAppFromSectionUseCase(membershipRepository)
    val moveAppToSectionUseCase = MoveAppToSectionUseCase(sectionRepository, membershipRepository)
    val sortAppsUseCase = SortAppsUseCase()

    val viewModelFactory = AppViewModelFactory(this)

    fun addAppsViewModelFactory(sectionId: String): AppViewModelFactory =
        AppViewModelFactory(this, sectionId)
}
