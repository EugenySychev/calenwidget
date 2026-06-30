package com.sychev.calenwidget

import android.content.Context

object WidgetPrefs {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_BG_ALPHA = "bg_alpha"
    private const val KEY_TEXT_COLOR = "text_color"

    fun getBackgroundAlpha(context: Context): Float =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_BG_ALPHA, 0.75f)

    fun setBackgroundAlpha(context: Context, alpha: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_BG_ALPHA, alpha).apply()
    }

    fun getTextColor(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_TEXT_COLOR, 0xFFFFFFFF.toInt())

    fun setTextColor(context: Context, colorArgb: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_TEXT_COLOR, colorArgb).apply()
    }

    fun getBackgroundColor(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BG_COLOR, 0xFF000000.toInt())

    fun setBackgroundColor(context: Context, colorArgb: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_BG_COLOR, colorArgb).apply()
    }

    private const val KEY_BG_COLOR = "bg_color"
}
