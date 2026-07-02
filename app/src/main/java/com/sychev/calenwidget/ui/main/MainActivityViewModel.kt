package com.sychev.calenwidget.ui.main

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sychev.calenwidget.CalendarRepository
import com.sychev.calenwidget.CalendarWidget
import com.sychev.calenwidget.WidgetPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarRepository: CalendarRepository,
    private val widgetPrefs: WidgetPrefs,
) : ViewModel(){
    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun onActions(action: MainActivityActions) {
        when (action) {
            is MainActivityActions.UpdateFontSize -> {
                widgetPrefs.setFontSize(context, action.fontSize)
                fetchData()
            }
            is MainActivityActions.UpdateBackgroundAlpha -> {
                widgetPrefs.setBackgroundAlpha(context, action.alpha)
                fetchData()
            }
            is MainActivityActions.CalendarClicked -> {

            }
            is MainActivityActions.UpdateBackgroundColor -> {
                widgetPrefs.setBackgroundColor(context, action.colorArgb)
                fetchData()
            }
            is MainActivityActions.UpdateTextColor -> {
                widgetPrefs.setTextColor(context, action.colorArgb)
                fetchData()
            }
        }
    }
    private fun fetchData() {
        updateWidgetUiState()
        updateAppWidget()
        updateCalendarsList()
    }

    private fun updateCalendarsList() {
        val calendars = calendarRepository.getCalendars().map { calendarInfo ->
            val selected = uiState.value.calendars
                .find{ it.id == calendarInfo.id }
                ?.selected ?: true
            calendarInfo.copy(
                selected = selected,
            )
        }
        _uiState.update {
            it.copy(
                calendars = calendars,
            )
        }
    }
    private fun updateAppWidget() {
        viewModelScope.launch {
            CalendarWidget().updateAll(context)
        }
    }

    private fun updateWidgetUiState() {
        _uiState.update { state ->
            state.copy(
                widgetParams = WidgetParams(
                    fontSize = widgetPrefs.getFontSize(),
                    bgAlpha = widgetPrefs.getBackgroundAlpha(),
                    bgColorArgb = widgetPrefs.getBackgroundColor(),
                    textColorArgb = widgetPrefs.getTextColor(),
                ),
                calendars = widgetPrefs.getSelectedCalendars(),
            )
        }
    }
}