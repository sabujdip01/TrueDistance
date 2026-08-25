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
 * §6.1.4 / §6.2.1 — LocationTrackingHelper wraps Google FusedLocationProviderClient into a cold Coroutine Flow.
 *
 * Responsibilities:
 * 1. Maps application GpsAccuracyMode settings into Google Play Services Priority levels.
 * 2. Emits continuous Location fixes at the requested intervalSeconds frequency.
 * 3. Handles clean registration and unregistration of LocationCallback upon Flow completion/cancellation.
 */
class LocationTrackingHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Observes real-time location stream as a reactive Kotlin Flow.
     */
    @SuppressLint("MissingPermission") // Checked upstream by LocationPermissionHelper
    fun observeLocation(
        intervalSeconds: Int,
        accuracyMode: GpsAccuracyMode
    ): Flow<android.location.Location> = callbackFlow {
        val priority = when (accuracyMode) {
            GpsAccuracyMode.HIGH -> Priority.PRIORITY_HIGH_ACCURACY
            GpsAccuracyMode.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            GpsAccuracyMode.DEVICE_ONLY -> Priority.PRIORITY_HIGH_ACCURACY
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
