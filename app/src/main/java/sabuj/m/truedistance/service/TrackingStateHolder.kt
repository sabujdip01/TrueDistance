package sabuj.m.truedistance.service

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class TrackingState(
    val currentLocation: LatLng? = null,
    val destination: LatLng? = null,
    val destinationName: String = "",
    val distanceMeters: Double = 0.0,
    val formattedDistance: String = "",
    val isTracking: Boolean = false,
    val staleFix: Boolean = false
)

/**
 * §6.1.4 / §12 — single source of truth shared between TrackingService (which owns
 * the actual location loop) and the UI (TrackingViewModel/Fragment), so tracking can
 * keep running via the foreground service even if the Fragment is destroyed.
 */
@Singleton
class TrackingStateHolder @Inject constructor() {
    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    fun update(transform: (TrackingState) -> TrackingState) {
        _state.value = transform(_state.value)
    }
}
