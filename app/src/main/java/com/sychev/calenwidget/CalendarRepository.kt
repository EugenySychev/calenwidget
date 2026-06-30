package com.sychev.calenwidget

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import java.util.Calendar

class CalendarRepository(private val context: Context) {

    private val MAX_EVENTS_COUNT = 10
    private val PROJECTION = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.ALL_DAY,
        CalendarContract.Instances.CALENDAR_COLOR
    )

    fun getEvents(): List<CalendarEvent> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        cal.apply {
            add(Calendar.YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val endOfDay = cal.timeInMillis

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, startOfDay)
            ContentUris.appendId(it, endOfDay)
        }.build()

        val events = mutableListOf<CalendarEvent>()
        val cursor = context.contentResolver.query(
            uri, PROJECTION, null, null, "${CalendarContract.Instances.BEGIN} ASC"
        ) ?: return events

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val title = it.getString(1) ?: continue
                val begin = it.getLong(2)
                val end = it.getLong(3)
                val allDay = it.getInt(4) == 1
                val color = it.getInt(5)
                val date = Calendar.getInstance().run {
                    timeInMillis = begin
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    time
                }
                events.add(CalendarEvent(id, title, date, begin, end, allDay, color))
                if (events.size >= MAX_EVENTS_COUNT) {
                    return events
                }
            }
        }
        return events
    }
}
