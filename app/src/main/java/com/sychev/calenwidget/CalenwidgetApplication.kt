package com.sychev.calenwidget

import android.app.Application

class CalenwidgetApplication : Application() {

    val calendarRepository: CalendarRepository by lazy { CalendarRepository(this) }

    companion object {
        lateinit var instance: CalenwidgetApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
