package com.strataguard.app.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberLocationProvider(onResult: (LocationResult) -> Unit): LocationProvider {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation(context, onResult) else onResult(LocationResult.PermissionDenied)
    }

    return remember(onResult) {
        object : LocationProvider {
            override fun requestLocation() {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) fetchLocation(context, onResult)
                else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocation(context: Context, onResult: (LocationResult) -> Unit) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    if (last != null) {
        onResult(LocationResult.Success(DeviceLocation(last.latitude, last.longitude)))
        return
    }
    val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            onResult(LocationResult.Success(DeviceLocation(loc.latitude, loc.longitude)))
            lm.removeUpdates(this)
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }
    try {
        lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper())
    } catch (e: Exception) {
        onResult(LocationResult.Unavailable)
    }
}
