package sabuj.m.truedistance.utils

import android.content.Context
import android.location.LocationManager

/** §11.2 — checks whether device location services are enabled. */
object GpsStatusHelper {
    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
