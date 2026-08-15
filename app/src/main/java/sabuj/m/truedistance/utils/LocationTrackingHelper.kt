package sabuj.m.truedistance.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import sabuj.m.truedistance.database.GpsAccuracyMode

/**
 * §6.1.4 / §6.2.1 — continuous location listener (not a one-shot fetch), per §12
 * Tech Notes. Interval + accuracy mode both come from Settings (§6.3.1).
 */
class LocationTrackingHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // caller must check LocationPermissionHelper first
    fun observeLocation(
        intervalSeconds: Int,
        accuracyMode: GpsAccuracyMode
    ): Flow<android.location.Location> = callbackFlow {
        val priority = when (accuracyMode) {
            GpsAccuracyMode.HIGH -> Priority.PRIORITY_HIGH_ACCURACY
            GpsAccuracyMode.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            GpsAccuracyMode.DEVICE_ONLY -> Priority.PRIORITY_HIGH_ACCURACY // GPS-only handled via provider filtering upstream
        }

        val request = LocationRequest.Builder(priority, intervalSeconds * 1000L)
            .setMinUpdateIntervalMillis(intervalSeconds * 1000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, context.mainLooper)

        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }
}
