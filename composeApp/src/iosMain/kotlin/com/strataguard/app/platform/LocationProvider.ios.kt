package com.strataguard.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject

@Composable
actual fun rememberLocationProvider(onResult: (LocationResult) -> Unit): LocationProvider {
    val delegate = remember { IosLocationDelegate(onResult) }
    val manager = remember {
        CLLocationManager().also {
            it.delegate = delegate
            it.desiredAccuracy = 100.0 // 100m accuracy is fine for nearby search
        }
    }
    delegate.onResult = onResult

    return remember {
        object : LocationProvider {
            override fun requestLocation() {
                when (CLLocationManager.authorizationStatus()) {
                    kCLAuthorizationStatusNotDetermined -> {
                        delegate.pendingRequest = true
                        manager.requestWhenInUseAuthorization()
                    }
                    kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                        onResult(LocationResult.PermissionDenied)
                    }
                    else -> manager.requestLocation()
                }
            }
        }
    }
}

private class IosLocationDelegate(
    var onResult: (LocationResult) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    var pendingRequest: Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        val lat = loc.coordinate.useContents { latitude }
        val lng = loc.coordinate.useContents { longitude }
        onResult(LocationResult.Success(DeviceLocation(lat, lng)))
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onResult(LocationResult.Unavailable)
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        if (!pendingRequest) return
        pendingRequest = false
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> manager.requestLocation()
            else -> onResult(LocationResult.PermissionDenied)
        }
    }
}
