package sabuj.m.truedistance.ui.distance

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.service.TrackingService
import sabuj.m.truedistance.service.TrackingState
import sabuj.m.truedistance.service.TrackingStateHolder
import javax.inject.Inject

/**
 * §6.1.4 Tracking Screen ViewModel — Bridges [TrackingFragment] with [TrackingService] via [TrackingStateHolder],
 * handling service startup, synchronous state reset, and stopping.
 */
@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateHolder: TrackingStateHolder
) : ViewModel() {

    /** Exposes reactive tracking metrics from [TrackingStateHolder]. */
    val uiState: StateFlow<TrackingState> = stateHolder.state

    fun startTracking(destinationName: String, destLat: Double, destLng: Double, savedLocationId: String?) {
        // Reset state synchronously BEFORE the service starts, so the fragment
        // never observes stale markers from the previous tracking session.
        stateHolder.reset()

        val intent = Intent(context, TrackingService::class.java).apply {
            putExtra(TrackingService.EXTRA_DEST_NAME, destinationName)
            putExtra(TrackingService.EXTRA_DEST_LAT, destLat)
            putExtra(TrackingService.EXTRA_DEST_LNG, destLng)
            savedLocationId?.let { putExtra(TrackingService.EXTRA_SAVED_LOCATION_ID, it) }
        }
        context.startForegroundService(intent)
    }

    /** §6.1.4 — Stop Tracking button (explicit user action). */
    fun stopTracking() {
        context.startService(
            Intent(context, TrackingService::class.java).setAction(TrackingService.ACTION_STOP)
        )
    }
}
