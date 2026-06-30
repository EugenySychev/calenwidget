package com.sychev.calenwidget

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = CalendarRepository.getTodayEvents(context)
        provideContent {
            WidgetContent(events)
        }
    }
}

@Composable
private fun WidgetContent(events: List<CalendarEvent>) {
    val context = LocalContext.current
    val dayStr = DateFormat.format("EEE, d MMM", Calendar.getInstance().time).toString()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .padding(8.dp)
    ) {
        Text(
            text = dayStr,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            modifier = GlanceModifier.padding(bottom = 6.dp)
        )
        if (events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.getString(R.string.no_event_for_today),
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(events, itemId = { it.id }) { event ->
                    EventRow(event)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent) {
    val context = LocalContext.current
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = if (event.allDay) context.getString(R.string.all_day)
    else "${timeFmt.format(Date(event.startTime))} – ${timeFmt.format(Date(event.endTime))}"

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeStr,
            style = TextStyle(fontSize = 11.sp),
            modifier = GlanceModifier.padding(end = 8.dp)
        )
        Text(
            text = event.title,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
        )
    }
}
