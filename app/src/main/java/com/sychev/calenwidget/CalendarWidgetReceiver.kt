package com.sychev.calenwidget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    // Tell the receiver which GlanceAppWidget to display
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}