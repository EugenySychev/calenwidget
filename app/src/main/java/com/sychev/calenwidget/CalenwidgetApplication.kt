package com.sychev.calenwidget

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CalenwidgetApplication : Application() {

    companion object {
        lateinit var instance: CalenwidgetApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
