package com.sychev.calenwidget.ui.main

sealed class MainActivityActions {
    data class CalendarClicked(val calendarId: String) : MainActivityActions()

}