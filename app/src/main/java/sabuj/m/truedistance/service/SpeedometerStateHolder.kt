package sabuj.m.truedistance.service

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SpeedometerState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val currentSpeedMps: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val averageSpeedMps: Double = 0.0,
    val distanceCoveredMeters: Double = 0.0,
    val startedAtMillis: Long = 0L,
    val elapsedMillis: Long = 0L,
    val currentLocation: LatLng? = null,
    val pathPoints: List<LatLng> = emptyList(),
    val staleFix: Boolean = false
)

/**
 * §6.2 — Single source of truth shared between SpeedometerService and SpeedometerViewModel/Fragment.
 */
@Singleton
class SpeedometerStateHolder @Inject constructor() {
    private val _state = MutableStateFlow(SpeedometerState())
    val state: StateFlow<SpeedometerState> = _state.asStateFlow()

    fun update(transform: (SpeedometerState) -> SpeedometerState) {
        _state.value = transform(_state.value)
    }

    fun reset() {
        _state.value = SpeedometerState()
    }
}
