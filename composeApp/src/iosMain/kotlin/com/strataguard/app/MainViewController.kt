package com.strataguard.app

import androidx.compose.ui.window.ComposeUIViewController
import com.strataguard.app.platform.requestNotificationPermission

fun MainViewController() = ComposeUIViewController {
    requestNotificationPermission()
    App()
}
