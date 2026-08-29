package sabuj.m.truedistance.utils

import android.content.Context
import android.location.LocationManager

/**
 * §11.2 — Utility helper verifying whether system location services (GPS or Network provider) are enabled.
 */
object GpsStatusHelper {
    /**
     * Checks if either GPS_PROVIDER or NETWORK_PROVIDER is currently active on the device.
     */
    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
