package com.sychev.calenwidget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sychev.calenwidget.ui.theme.CalenwidgetTheme

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
                            Text("Доступ к календарю разрешён. Добавьте виджет на рабочий стол.")
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
}
