package com.iamacat.appbox.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.iamacat.appbox.data.model.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Discovers launchable applications through PackageManager and keeps a bounded
 * in-memory icon cache. Icons are never persisted.
 */
class InstalledAppsRepository(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    private val _installedApps = MutableStateFlow<List<AppEntry>>(emptyList())
    val installedApps: StateFlow<List<AppEntry>> = _installedApps.asStateFlow()

    private val iconSizePx =
        (56f * appContext.resources.displayMetrics.density).roundToInt().coerceAtLeast(56)

    private val maxCacheBytes: Int = (Runtime.getRuntime().maxMemory() / 8L)
        .coerceAtMost(24L * 1024 * 1024)
        .toInt()

    private val iconCache = object : LruCache<String, ImageBitmap>(maxCacheBytes) {
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            value.width * value.height * 4
    }

    suspend fun refresh() {
        val apps = withContext(Dispatchers.IO) { loadLaunchableApps() }
        iconCache.evictAll()
        _installedApps.value = apps
    }

    private fun loadLaunchableApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        val ownPackage = appContext.packageName
        return resolveInfos.asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { it.packageName != ownPackage }
            .distinctBy { it.packageName }
            .map { info ->
                AppEntry(
                    packageName = info.packageName,
                    label = runCatching { info.loadLabel(packageManager).toString() }
                        .getOrDefault(info.packageName)
                        .ifBlank { info.packageName }
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            .toList()
    }

    suspend fun loadIcon(packageName: String): ImageBitmap? {
        iconCache.get(packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            iconCache.get(packageName) ?: runCatching {
                packageManager.getApplicationIcon(packageName)
                    .toBitmap(iconSizePx, iconSizePx)
                    .asImageBitmap()
            }.getOrNull()?.also { iconCache.put(packageName, it) }
        }
    }

    fun isLaunchable(packageName: String): Boolean =
        packageManager.getLaunchIntentForPackage(packageName) != null
}
