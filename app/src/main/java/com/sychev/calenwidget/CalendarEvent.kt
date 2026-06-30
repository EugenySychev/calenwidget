package com.sychev.calenwidget

import java.util.Date

data class CalendarEvent(
    val id: Long,
    val title: String,
    val date: Date,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val calendarColor: Int
)
