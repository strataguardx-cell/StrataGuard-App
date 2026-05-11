package com.strataguard.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StrataColorScheme = lightColorScheme(
    primary            = Navy800,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    primaryContainer   = Navy100,
    onPrimaryContainer = Navy900,
    secondary          = Amber500,
    onSecondary        = Navy900,
    secondaryContainer = Amber100,
    onSecondaryContainer = Navy800,
    background         = Grey50,
    onBackground       = Grey900,
    surface            = androidx.compose.ui.graphics.Color.White,
    onSurface          = Grey900,
    surfaceVariant     = Navy50,
    onSurfaceVariant   = Grey700,
    outline            = Grey300,
    outlineVariant     = Grey200,
    error              = ErrorRed,
    onError            = androidx.compose.ui.graphics.Color.White,
    errorContainer     = ErrorLight,
    onErrorContainer   = ErrorRed,
)

@Composable
fun StrataGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StrataColorScheme,
        typography  = StrataTypography,
        content     = content,
    )
}