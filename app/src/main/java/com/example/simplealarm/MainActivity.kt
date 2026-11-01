package com.example.simplealarm

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmSetupScreen()
                }
            }
        }
    }
}

@Composable
fun AlarmSetupScreen() {
    val context = LocalContext.current
    var selectedHour by remember { mutableStateOf(5) }
    var selectedMinute by remember { mutableStateOf(0) }
    var repetitions by remember { mutableStateOf(5) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setel Alarm Sekuensial",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedHour = hourOfDay
                        selectedMinute = minute
                    },
                    selectedHour,
                    selectedMinute,
                    true
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            Text(text = "Waktu Pemicu: $formattedTime", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Jumlah Berdering: $repetitions Kali (Tiap 1 Menit)")
        Slider(
            value = repetitions.toFloat(),
            onValueChange = { repetitions = it.toInt() },
            valueRange = 1f..15f,
            steps = 14
        )

        val endMinuteCalc = selectedMinute + repetitions
        val previewEndMin = endMinuteCalc % 60
        val previewEndHour = (selectedHour + (endMinuteCalc / 60)) % 24
        val formattedStart = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, (selectedMinute + 1) % 60)
        val formattedEnd = String.format(Locale.getDefault(), "%02d:%02d", previewEndHour, previewEndMin)

        Text(
            text = "Alarm berdering dari $formattedStart s.d $formattedEnd",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        Toast.makeText(context, "Izinkan alarm presisi terlebih dahulu.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                }

                AlarmScheduler.scheduleSequentialAlarms(
                    context = context,
                    hour = selectedHour,
                    minute = selectedMinute,
                    repetitions = repetitions
                )

                Toast.makeText(
                    context,
                    "$repetitions alarm berhasil didaftarkan!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "AKTIFKAN RENTETAN ALARM", fontWeight = FontWeight.Bold)
        }
    }
}
