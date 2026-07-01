package com.sychev.calenwidget.ui.main

import com.sychev.calenwidget.CalendarInfo

data class WidgetParams(
    val fontSize: Int,
    val bgColorArgb: Int,
    val bgAlpha: Float,
    val textColorArgb: Int,
)

internal data class MainActivityUiState(
    val calendars: List<CalendarInfo> = emptyList(),
    val widgetParams: WidgetParams,
)
