package com.strataguard.app.platform

import androidx.compose.runtime.Composable

data class DeviceLocation(val latitude: Double, val longitude: Double)

sealed class LocationResult {
    data class Success(val location: DeviceLocation) : LocationResult()
    object PermissionDenied : LocationResult()
    object Unavailable : LocationResult()
}

interface LocationProvider {
    fun requestLocation()
}

@Composable
expect fun rememberLocationProvider(onResult: (LocationResult) -> Unit): LocationProvider
