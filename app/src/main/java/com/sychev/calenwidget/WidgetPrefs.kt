package com.sychev.calenwidget

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val PREFS_NAME = "widget_prefs"
private const val KEY_BG_ALPHA = "bg_alpha"
private const val KEY_TEXT_COLOR = "text_color"
private const val KEY_FONT_SIZE = "font_size"
private const val KEY_SELECTED_CALENDARS = "selected_calendars"
private const val KEY_BG_COLOR = "bg_color"

class WidgetPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getBackgroundAlpha(): Float =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_BG_ALPHA, 0.75f)

    fun setBackgroundAlpha(context: Context, alpha: Float) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putFloat(KEY_BG_ALPHA, alpha)
            }
    }

    fun getFontSize(): Int =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FONT_SIZE, 12 )

    fun setFontSize(context: Context, size: Int) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt(KEY_FONT_SIZE, size)
            }
    }
    fun getTextColor(): Int =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_TEXT_COLOR, 0xFFFFFFFF.toInt())

    fun setTextColor(context: Context, colorArgb: Int) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putInt(KEY_TEXT_COLOR, colorArgb) }
    }

    fun getBackgroundColor(): Int =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BG_COLOR, 0xFF000000.toInt())

    fun setBackgroundColor(context: Context, colorArgb: Int) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt(KEY_BG_COLOR, colorArgb)
            }
    }

    fun getSelectedCalendars(): List<CalendarInfo> {
        val json = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_CALENDARS, null) ?: return emptyList()
        return Json.decodeFromString(json)
    }

    fun setSelectedCalendars(context: Context, calendars: List<CalendarInfo>) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_SELECTED_CALENDARS, Json.encodeToString(calendars))
            }
    }
}
