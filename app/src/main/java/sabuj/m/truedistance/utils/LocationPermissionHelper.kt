package sabuj.m.truedistance.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * §10 Permissions — Utility methods checking foreground location, background location, and POST_NOTIFICATIONS permissions.
 */
object LocationPermissionHelper {

    /** Checks if ACCESS_FINE_LOCATION permission has been granted. */
    fun hasForegroundLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /** Checks if ACCESS_BACKGROUND_LOCATION permission has been granted. */
    fun hasBackgroundLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /** Checks if POST_NOTIFICATIONS runtime permission has been granted (Android 13+). */
    fun hasNotificationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
}
