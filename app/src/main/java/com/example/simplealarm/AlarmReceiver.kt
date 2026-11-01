package com.example.simplealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val index = intent.getIntExtra("EXTRA_ALARM_INDEX", 1)
        val total = intent.getIntExtra("EXTRA_TOTAL_COUNT", 1)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra("EXTRA_ALARM_INDEX", index)
            putExtra("EXTRA_TOTAL_COUNT", total)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
