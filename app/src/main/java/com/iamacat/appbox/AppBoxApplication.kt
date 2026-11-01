package com.iamacat.appbox

import android.app.Application
import com.iamacat.appbox.di.AppContainer

class AppBoxApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
