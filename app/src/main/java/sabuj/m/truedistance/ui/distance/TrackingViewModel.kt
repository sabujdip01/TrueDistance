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
 * §6.1.4 — thin wrapper around TrackingService: starts/stops the foreground service
 * and exposes its published TrackingStateHolder state to the UI. Actual location
 * tracking + Room writes live in the service so they survive backgrounding (§12).
 */
@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    stateHolder: TrackingStateHolder
) : ViewModel() {

    val uiState: StateFlow<TrackingState> = stateHolder.state

    fun startTracking(destinationName: String, destLat: Double, destLng: Double, savedLocationId: String?) {
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

    /**
     * §6.1.4 / §6.3.1 — called from Fragment.onStop(): if Background Tracking is
     * disabled in Settings, leaving the screen should stop tracking rather than
     * continue silently.
     */
    suspend fun stopIfBackgroundTrackingDisabled() {
        val enabled = settingsRepository.backgroundTrackingEnabled
        val isEnabled = enabled.first()
        if (!isEnabled) stopTracking()
    }
}
