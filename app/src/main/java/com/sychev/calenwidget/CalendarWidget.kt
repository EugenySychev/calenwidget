package com.sychev.calenwidget

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.text.format.DateFormat
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sychev.calenwidget.ui.main.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

const val LOG = "Widget"

private sealed interface WidgetListItem {
    data class DateHeader(val date: Date) : WidgetListItem
    data class EventItem(val event: CalendarEvent) : WidgetListItem
}

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface CalendarWidgetEntryPoint {
    fun calendarRepository(): CalendarRepository
}

class CalendarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetPrefs = WidgetPrefs(context)
        val calendarRepository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CalendarWidgetEntryPoint::class.java
        ).calendarRepository()
        val visibleCalendarIds = calendarRepository.getCalendars().map { it.id }.toSet()
        val events = calendarRepository.getEvents()
            .filter { it.calendarId in visibleCalendarIds }
            .take(CalendarRepository.MAX_EVENTS_COUNT)
        val bgAlpha = widgetPrefs.getBackgroundAlpha()
        val bgColorArgb = widgetPrefs.getBackgroundColor()
        val textColorArgb = widgetPrefs.getTextColor()
        val fontSize = widgetPrefs.getFontSize()
        provideContent {
            WidgetContent(events, bgAlpha, bgColorArgb, textColorArgb, fontSize)
        }
    }
}

class UpdateWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        CalendarWidget().updateAll(context)
    }
}

@Composable
private fun WidgetContent(
    events: List<CalendarEvent>,
    bgAlpha: Float,
    bgColorArgb: Int,
    textColorArgb: Int,
    fontSize: Int,
) {
    val context = LocalContext.current
    val textColor = ColorProvider(Color(textColorArgb))
    val bgColor = Color(bgColorArgb).copy(alpha = bgAlpha)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionRunCallback<UpdateWidgetAction>())
            .padding(8.dp)
    ) {
        if (events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.getString(R.string.no_event_for_today),
                    style = TextStyle(fontSize = fontSize.sp, color = textColor)
                )
            }
        } else {
            val listItems = events
                .groupBy { it.date }
                .flatMap { (date, group) ->
                    listOf(WidgetListItem.DateHeader(date)) +
                            group.map { WidgetListItem.EventItem(it) }
                }

            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(
                    items = listItems,
                    itemId = { item ->
                        when (item) {
                            is WidgetListItem.DateHeader -> item.date.time
                            is WidgetListItem.EventItem -> item.event.id
                        }
                    }
                ) { item ->
                    when (item) {
                        is WidgetListItem.DateHeader ->
                            DateGroupHeader(item.date, textColor, fontSize)
                        is WidgetListItem.EventItem ->
                            EventRow(item.event, textColor, fontSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateGroupHeader(date: Date, textColor: ColorProvider, fontSize: Int) {
    val context = LocalContext.current
    val openAppIntent = Intent(context, MainActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

    Text(
        text = DateFormat.format("d MMM, EEE", date).toString(),
        style = TextStyle(
            fontSize = (fontSize + 1).sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        ),
        modifier = GlanceModifier
            .padding(top = 6.dp, bottom = 2.dp)
            .clickable(actionStartActivity(openAppIntent))
    )
}

@Composable
private fun EventRow(event: CalendarEvent, textColor: ColorProvider, fontSize: Int) {
    val context = LocalContext.current
    val timeFmt = SimpleDateFormat("HH:mm", context.resources.configuration.locales[0])
    val timeStr = if (event.allDay) context.getString(R.string.all_day)
    else "${timeFmt.format(Date(event.startTime))} – ${timeFmt.format(Date(event.endTime))}"

    val eventUri = ContentUris.withAppendedId(
        CalendarContract.Events.CONTENT_URI,
        event.id,
    )
    val openIntent = Intent(Intent.ACTION_VIEW)
        .setData(eventUri)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
            .clickable(actionStartActivity(openIntent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeStr,
            style = TextStyle(fontSize = (fontSize - 1).sp, color = textColor),
            modifier = GlanceModifier.padding(end = 8.dp)
        )
        Text(
            text = event.title,
            style = TextStyle(
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        )
    }
}
