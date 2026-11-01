package com.iamacat.appbox.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iamacat.appbox.data.model.AppSettings
import com.iamacat.appbox.data.model.SortMode
import com.iamacat.appbox.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.appSettingsStore

    val settings: Flow<AppSettings> = store.data.map { preferences -> preferences.toSettings() }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setShowSubtitles(value: Boolean) = edit { it[SHOW_SUBTITLES] = value }

    suspend fun setSortMode(value: SortMode) = edit { it[SORT_MODE] = value.key }

    suspend fun setConfirmBeforeRemove(value: Boolean) = edit { it[CONFIRM_BEFORE_REMOVE] = value }

    suspend fun setThemeMode(value: ThemeMode) = edit { it[THEME_MODE] = value.key }

    suspend fun setLastSectionId(sectionId: String) = edit { it[LAST_SECTION_ID] = sectionId }

    suspend fun restoreDefaults() {
        store.edit { preferences ->
            preferences.remove(SHOW_SUBTITLES)
            preferences.remove(SORT_MODE)
            preferences.remove(CONFIRM_BEFORE_REMOVE)
            preferences.remove(THEME_MODE)
        }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        store.edit { preferences -> block(preferences) }
    }

    private fun Preferences.toSettings(): AppSettings = AppSettings(
        showSubtitles = this[SHOW_SUBTITLES] ?: true,
        sortMode = SortMode.fromKey(this[SORT_MODE]),
        confirmBeforeRemove = this[CONFIRM_BEFORE_REMOVE] ?: true,
        themeMode = ThemeMode.fromKey(this[THEME_MODE]),
        lastSectionId = this[LAST_SECTION_ID]
    )

    private companion object {
        val SHOW_SUBTITLES = booleanPreferencesKey("show_subtitles")
        val SORT_MODE = stringPreferencesKey("sort_mode")
        val CONFIRM_BEFORE_REMOVE = booleanPreferencesKey("confirm_before_remove")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_SECTION_ID = stringPreferencesKey("last_section_id")
    }
}
