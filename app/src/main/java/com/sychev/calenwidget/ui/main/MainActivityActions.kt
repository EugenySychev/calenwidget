package com.sychev.calenwidget.ui.main

sealed class MainActivityActions {
    data class CalendarClicked(val calendarId: String) : MainActivityActions()
    data class UpdateFontSize(val fontSize: Int) : MainActivityActions()
    data class UpdateBackgroundAlpha(val alpha: Float) : MainActivityActions()
    data class UpdateBackgroundColor(val colorArgb: Int) : MainActivityActions()
    data class UpdateTextColor(val colorArgb: Int) : MainActivityActions()
}