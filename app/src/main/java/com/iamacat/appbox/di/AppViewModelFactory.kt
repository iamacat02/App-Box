package com.iamacat.appbox.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iamacat.appbox.ui.viewmodel.AddAppsViewModel
import com.iamacat.appbox.ui.viewmodel.MainViewModel
import com.iamacat.appbox.ui.viewmodel.ManageSectionsViewModel
import com.iamacat.appbox.ui.viewmodel.SettingsViewModel

class AppViewModelFactory(
    private val container: AppContainer,
    private val sectionId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(
                appRepository = container.appRepository,
                installedAppsRepository = container.installedAppsRepository,
                settingsRepository = container.settingsRepository,
                appLauncher = container.appLauncher,
                observeSections = container.observeSectionsUseCase,
                createSection = container.createSectionUseCase,
                sortApps = container.sortAppsUseCase,
                addAppsToSection = container.addAppsToSectionUseCase,
                removeApp = container.removeAppFromSectionUseCase,
                moveApp = container.moveAppToSectionUseCase
            )

            modelClass.isAssignableFrom(AddAppsViewModel::class.java) -> AddAppsViewModel(
                sectionId = requireNotNull(sectionId) { "AddAppsViewModel requires a sectionId" },
                sectionRepository = container.sectionRepository,
                membershipRepository = container.membershipRepository,
                installedAppsRepository = container.installedAppsRepository,
                addAppsToSection = container.addAppsToSectionUseCase
            )

            modelClass.isAssignableFrom(ManageSectionsViewModel::class.java) -> ManageSectionsViewModel(
                sectionRepository = container.sectionRepository,
                observeSections = container.observeSectionsUseCase,
                createSection = container.createSectionUseCase,
                updateSection = container.updateSectionUseCase,
                deleteSection = container.deleteSectionUseCase,
                deleteSections = container.deleteSectionsUseCase
            )

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
                settingsRepository = container.settingsRepository,
                sectionRepository = container.sectionRepository
            )

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
        return viewModel as T
    }
}
