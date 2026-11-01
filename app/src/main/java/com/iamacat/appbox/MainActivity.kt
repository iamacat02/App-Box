package com.iamacat.appbox

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamacat.appbox.data.model.AppSettings
import com.iamacat.appbox.navigation.AppNavigation
import com.iamacat.appbox.ui.component.LocalAppIconLoader
import com.iamacat.appbox.ui.theme.AppBoxTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        val container = (application as AppBoxApplication).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            AppBoxTheme(themeMode = settings.themeMode) {
                CompositionLocalProvider(
                    LocalAppIconLoader provides { packageName ->
                        container.installedAppsRepository.loadIcon(packageName)
                    }
                ) {
                    AppNavigation(container = container)
                }
            }
        }
    }
}
