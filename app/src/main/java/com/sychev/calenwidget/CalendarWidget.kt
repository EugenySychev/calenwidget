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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


const val LOG = "Widget"

class CalendarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = CalendarRepository.getTodayEvents(context)
        val bgAlpha = WidgetPrefs.getBackgroundAlpha(context)
        val bgColorArgb = WidgetPrefs.getBackgroundColor(context)
        val textColorArgb = WidgetPrefs.getTextColor(context)
        provideContent {
            WidgetContent(events, bgAlpha, bgColorArgb, textColorArgb)
        }
    }
}

class UpdateWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(LOG, "Widget action")
        CalendarWidget().updateAll(context)
    }
}

@Composable
private fun WidgetContent(
    events: List<CalendarEvent>,
    bgAlpha: Float,
    bgColorArgb: Int,
    textColorArgb: Int
) {
    val context = LocalContext.current
    val textColor = ColorProvider(Color(textColorArgb))
    val bgColor = Color(bgColorArgb).copy(alpha = bgAlpha)
    val dayStr = DateFormat.format("EEE, d MMM", Calendar.getInstance().time).toString()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionRunCallback<UpdateWidgetAction>())
            .padding(8.dp)
    ) {
        Text(
            text = dayStr,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor),
            modifier = GlanceModifier.padding(bottom = 6.dp)
        )
        if (events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.getString(R.string.no_event_for_today),
                    style = TextStyle(fontSize = 12.sp, color = textColor)
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(events, itemId = { it.id }) { event ->
                    EventRow(event, textColor)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, textColor: ColorProvider) {
    val context = LocalContext.current
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = if (event.allDay) context.getString(R.string.all_day)
    else "${timeFmt.format(Date(event.startTime))} – ${timeFmt.format(Date(event.endTime))}"

    val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
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
            style = TextStyle(fontSize = 11.sp, color = textColor),
            modifier = GlanceModifier.padding(end = 8.dp)
        )
        Text(
            text = event.title,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        )
    }
}
