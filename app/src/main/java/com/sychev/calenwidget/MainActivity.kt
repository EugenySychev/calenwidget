package com.sychev.calenwidget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import com.sychev.calenwidget.ui.theme.CalenwidgetTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private var calendarPermissionGranted by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            calendarPermissionGranted = granted
        }

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
            CalenwidgetTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                var bgAlpha by remember { mutableFloatStateOf(WidgetPrefs.getBackgroundAlpha(context)) }
                var bgColorArgb by remember { mutableIntStateOf(WidgetPrefs.getBackgroundColor(context)) }
                var textColorArgb by remember { mutableIntStateOf(WidgetPrefs.getTextColor(context)) }

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
                            Text("Доступ к календарю разрешён.")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { pinWidget() }) {
                                Text("Добавить виджет на рабочий стол")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Настройки виджета",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Цвет фона:",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                presetBgColors.forEach { color ->
                                    val isSelected = bgColorArgb == color.toArgb()
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
                                                bgColorArgb = color.toArgb()
                                                WidgetPrefs.setBackgroundColor(context, color.toArgb())
                                                scope.launch { CalendarWidget().updateAll(context) }
                                            }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Прозрачность фона: ${(bgAlpha * 100).roundToInt()}%",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Slider(
                                value = bgAlpha,
                                onValueChange = { bgAlpha = it },
                                onValueChangeFinished = {
                                    WidgetPrefs.setBackgroundAlpha(context, bgAlpha)
                                    scope.launch { CalendarWidget().updateAll(context) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Цвет текста:",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                presetColors.forEach { color ->
                                    val isSelected = textColorArgb == color.toArgb()
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
                                                textColorArgb = color.toArgb()
                                                WidgetPrefs.setTextColor(context, color.toArgb())
                                                scope.launch { CalendarWidget().updateAll(context) }
                                            }
                                    )
                                }
                            }
                        } else {
                            Text("Для работы виджета необходим доступ к календарю.")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                            }) {
                                Text("Разрешить доступ")
                            }
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
