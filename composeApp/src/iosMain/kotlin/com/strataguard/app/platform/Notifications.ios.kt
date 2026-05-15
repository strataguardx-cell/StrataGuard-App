package com.strataguard.app.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual fun scheduleDeadlineReminders(
    disputeId: String,
    disputeType: String,
    tribunal: String,
    deadlineIso: String,
) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    val authOptions = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

    center.requestAuthorizationWithOptions(authOptions) { granted, _ ->
        if (!granted) return@requestAuthorizationWithOptions

        val deadline = runCatching { LocalDate.parse(deadlineIso) }.getOrNull() ?: return@requestAuthorizationWithOptions
        val zone = TimeZone.currentSystemDefault()
        val deadlineEpoch = deadline.atStartOfDayIn(zone).epochSeconds.toDouble()
        val nowEpoch = Clock.System.now().epochSeconds.toDouble()

        val displayType = disputeType.replace('_', ' ')
            .split(' ').joinToString(" ") { w -> w.lowercase().replaceFirstChar(Char::uppercase) }

        val reminders = listOf(
            Pair("${disputeId}_7d", deadlineEpoch - 7 * 86400.0 - nowEpoch),
            Pair("${disputeId}_1d", deadlineEpoch - 86400.0 - nowEpoch),
        )

        reminders.forEachIndexed { index, (id, intervalSeconds) ->
            if (intervalSeconds > 60.0) {
                val days = if (index == 0) 7 else 1
                val title = if (days == 7) "Filing deadline in 7 days — $displayType"
                    else "File your $displayType dispute tomorrow"
                val body = if (days == 7) "Your $displayType dispute at $tribunal must be filed within 7 days."
                    else "Urgent: Your $displayType dispute at $tribunal is due tomorrow."
                scheduleNotification(center, id, title, body, intervalSeconds)
            }
        }
    }
}

actual fun cancelDeadlineReminders(disputeId: String) {
    UNUserNotificationCenter.currentNotificationCenter()
        .removePendingNotificationRequestsWithIdentifiers(
            listOf("${disputeId}_7d", "${disputeId}_1d"),
        )
}

fun requestNotificationPermission() {
    val authOptions = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
    UNUserNotificationCenter.currentNotificationCenter()
        .requestAuthorizationWithOptions(authOptions) { _, _ -> }
}

private fun scheduleNotification(
    center: UNUserNotificationCenter,
    identifier: String,
    title: String,
    body: String,
    intervalSeconds: Double,
) {
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(body)
        setSound(UNNotificationSound.defaultSound())
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(intervalSeconds, repeats = false)
    val request = UNNotificationRequest.requestWithIdentifier(identifier, content, trigger)
    center.addNotificationRequest(request) { _ -> }
}
