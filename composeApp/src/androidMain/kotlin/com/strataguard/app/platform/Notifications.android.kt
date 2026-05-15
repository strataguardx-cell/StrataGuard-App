package com.strataguard.app.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.strataguard.app.receiver.DeadlineNotificationReceiver
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

actual fun scheduleDeadlineReminders(
    disputeId: String,
    disputeType: String,
    tribunal: String,
    deadlineIso: String,
) {
    val ctx = androidAppContext ?: return
    val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val deadline = runCatching { LocalDate.parse(deadlineIso) }.getOrNull() ?: return
    val zone = ZoneId.systemDefault()
    val deadlineMillis = deadline.atStartOfDay(zone).toInstant().toEpochMilli()
    val now = System.currentTimeMillis()

    val displayType = disputeType.replace('_', ' ')
        .split(' ').joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

    val reminders = listOf(
        Triple(7, deadlineMillis - 7 * 86_400_000L, "Filing deadline in 7 days — $displayType"),
        Triple(1, deadlineMillis - 86_400_000L, "File your $displayType dispute tomorrow"),
    )

    reminders.forEach { (days, triggerAt, title) ->
        if (triggerAt > now) {
            val body = if (days == 7)
                "Your $displayType dispute at $tribunal must be filed within 7 days."
            else
                "Urgent: Your $displayType dispute at $tribunal is due tomorrow."
            schedule(ctx, alarmManager, pendingIntentCode(disputeId, days), triggerAt, title, body)
        }
    }
}

actual fun cancelDeadlineReminders(disputeId: String) {
    val ctx = androidAppContext ?: return
    val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    listOf(7, 1).forEach { days ->
        val intent = Intent(ctx, DeadlineNotificationReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx,
            pendingIntentCode(disputeId, days),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        pi?.let { alarmManager.cancel(it) }
    }
}

private fun schedule(
    ctx: Context,
    alarmManager: AlarmManager,
    requestCode: Int,
    triggerAt: Long,
    title: String,
    body: String,
) {
    val intent = Intent(ctx, DeadlineNotificationReceiver::class.java).apply {
        putExtra("notificationId", requestCode)
        putExtra("title", title)
        putExtra("body", body)
    }
    val pi = PendingIntent.getBroadcast(
        ctx, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
}

private fun pendingIntentCode(disputeId: String, days: Int) = abs(disputeId.hashCode()) * 10 + days
