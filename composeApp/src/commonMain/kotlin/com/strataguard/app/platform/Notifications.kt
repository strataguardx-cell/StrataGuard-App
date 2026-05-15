package com.strataguard.app.platform

expect fun scheduleDeadlineReminders(
    disputeId: String,
    disputeType: String,
    tribunal: String,
    deadlineIso: String,
)

expect fun cancelDeadlineReminders(disputeId: String)
