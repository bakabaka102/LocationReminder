package com.udacity.project4.utils

import android.Manifest
import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object RequestPermissionUtils {
}

fun Context.isAccessFineLocation(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.isBackgroundLocationEnable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.checkSelfPermission(
            this, permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun Context.isPermissionLocationGranted(): Boolean {
    return (ContextCompat.checkSelfPermission(
        this,
        permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(
        this,
        permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}


fun Context.isPostNotificationEnable(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}
