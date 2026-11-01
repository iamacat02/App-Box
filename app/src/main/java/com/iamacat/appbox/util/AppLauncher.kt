package com.iamacat.appbox.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

class AppLauncher(context: Context) {

    private val appContext = context.applicationContext

    fun openApp(packageName: String): Boolean {
        val intent = appContext.packageManager.getLaunchIntentForPackage(packageName)
        return launch(intent)
    }

    fun openAppInfo(packageName: String): Boolean {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        return launch(intent)
    }

    fun showMessage(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun launch(intent: Intent?): Boolean {
        if (intent == null) return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            appContext.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
