package com.strataguard.app

import androidx.compose.runtime.Composable
import com.strataguard.app.di.appModule
import com.strataguard.app.navigation.AppNavigation
import com.strataguard.app.ui.theme.StrataGuardTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        StrataGuardTheme {
            AppNavigation()
        }
    }
}