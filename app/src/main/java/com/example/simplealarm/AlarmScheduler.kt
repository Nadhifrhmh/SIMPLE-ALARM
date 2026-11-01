package com.example.simplealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    fun scheduleSequentialAlarms(context: Context, hour: Int, minute: Int, repetitions: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val baseCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val baseTimeMillis = baseCalendar.timeInMillis
        val groupBaseId = (baseTimeMillis / 60000 % 100000).toInt()

        for (i in 1..repetitions) {
            val triggerTime = baseCalendar.clone() as Calendar
            triggerTime.add(Calendar.MINUTE, i)

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("EXTRA_ALARM_INDEX", i)
                putExtra("EXTRA_TOTAL_COUNT", repetitions)
                putExtra("EXTRA_GROUP_ID", groupBaseId)
            }

            val requestCode = groupBaseId * 100 + i

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}
