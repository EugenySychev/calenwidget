package com.sychev.calenwidget.ui.main

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sychev.calenwidget.CalendarInfo
import com.sychev.calenwidget.CalendarWidgetReceiver
import com.sychev.calenwidget.R
import com.sychev.calenwidget.ui.theme.CalenwidgetTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var calendarPermissionGranted by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            calendarPermissionGranted = granted
        }

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendarPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        if (!calendarPermissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }

        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CalenwidgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (calendarPermissionGranted) {
                            WidgetSettings(uiState.widgetParams, viewModel::onActions)
                            Spacer(
                                modifier = Modifier
                                    .height(
                                        dimensionResource(R.dimen.spacer_height),
                                    ),
                            )
                            CalendarList(
                                uiState.calendars,
                                viewModel::onActions,
                            )
                            HorizontalDivider()
                            Spacer(
                                modifier = Modifier
                                    .height(
                                        dimensionResource(R.dimen.spacer_height),
                                    ),
                            )
                            WidgetPlacing()
                        } else {
                            Text(stringResource(R.string.calendar_permission_required))
                            Spacer(
                                modifier = Modifier
                                    .height(
                                        dimensionResource(R.dimen.spacer_height),
                                    ),
                            )
                            Button(onClick = {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                            }) {
                                Text(stringResource(R.string.allow_access))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetPlacing() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.calendar_access_granted))
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { pinWidget() }) {
                Text(stringResource(R.string.add_widget))
            }
        }
    }

    @Composable
    private fun CalendarList(
        calendars: List<CalendarInfo>,
        onActions: (MainActivityActions) -> Unit,
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            state = lazyListState,
        ) {
            items(
                items = calendars,
                key = { item -> item.id }
            ) { item ->
                Text(
                    text = item.displayName
                )
            }
        }

    }

    @Composable
    private fun WidgetSettings(
        widgetParams: WidgetParams,
        onActions: (MainActivityActions) -> Unit = {},
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        val presetBgColors = listOf(
            Color.Black,
            Color(0xFF37474F),
            Color(0xFF1A237E),
            Color(0xFF1B5E20),
            Color(0xFF4A148C),
        )

        val presetColors = listOf(
            Color.White,
            Color(0xFFCCCCCC),
            Color(0xFFFFEB3B),
            Color(0xFF4FC3F7),
            Color.Black,
        )
        val fontSizesRange = 0.10f..0.36f
        val fontSizesSteps = (fontSizesRange.endInclusive - fontSizesRange.start) / 0.02f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val titleStyle = MaterialTheme.typography.titleMedium
            val iconSize = with(LocalDensity.current) { titleStyle.fontSize.toDp() }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.widget_settings),
                    style = titleStyle,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    painter = painterResource(
                        id = if (isExpanded) {
                            R.drawable.up_arrow
                        } else {
                            R.drawable.down_arrow
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable {
                            isExpanded = !isExpanded
                        },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.bg_color_label),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetBgColors.forEach { color ->
                            val isSelected = widgetParams.bgColorArgb == color.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onActions(MainActivityActions.UpdateBackgroundColor(color.toArgb()))
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            R.string.bg_alpha_label,
                            (widgetParams.bgAlpha * 100).roundToInt()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = widgetParams.bgAlpha,
                        onValueChange = {
                            onActions(MainActivityActions.UpdateBackgroundAlpha(it))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.font_size_label, widgetParams.fontSize),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = widgetParams.fontSize.toFloat() / 100,
                        valueRange = fontSizesRange,
                        steps = fontSizesSteps.toInt(),
                        onValueChange = {
                            onActions(MainActivityActions.UpdateFontSize((it * 100).toInt()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.text_color_label),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetColors.forEach { color ->
                            val isSelected = widgetParams.textColorArgb == color.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onActions(MainActivityActions.UpdateTextColor(color.toArgb()))
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun pinWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        if (!appWidgetManager.isRequestPinAppWidgetSupported) return
        val provider = ComponentName(this, CalendarWidgetReceiver::class.java)
        appWidgetManager.requestPinAppWidget(provider, null, null)
    }
}
