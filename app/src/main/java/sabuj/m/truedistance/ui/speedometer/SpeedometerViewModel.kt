package sabuj.m.truedistance.ui.speedometer

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import sabuj.m.truedistance.database.AppSettingsKeys
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.service.SpeedometerService
import sabuj.m.truedistance.service.SpeedometerStateHolder
import sabuj.m.truedistance.utils.DistanceCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SpeedometerUiState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val formattedSpeed: String = "0.0",
    val speedUnitLabel: String = "km/h",
    val formattedDistance: String = "0.00 km",
    val formattedAvgSpeed: String = "0.0 km/h",
    val formattedMaxSpeed: String = "0.0 km/h",
    val formattedStartTime: String = "--:--:--",
    val formattedElapsedTime: String = "00:00:00",
    val currentLocation: LatLng? = null,
    val pathPoints: List<LatLng> = emptyList(),
    val staleFix: Boolean = false
)

@HiltViewModel
class SpeedometerViewModel @Inject constructor(
    private val stateHolder: SpeedometerStateHolder,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val timeFormatter = SimpleDateFormat("h:mm:ss a", Locale.US)

    val uiState: StateFlow<SpeedometerUiState> = combine(
        stateHolder.state,
        settingsRepository.unit,
        settingsRepository.decimalPrecision,
        settingsRepository.autoMetersUnder1km
    ) { state, unit, precision, autoMeters ->
        val speedUnit = when (unit) {
            sabuj.m.truedistance.database.UnitPreference.MILES -> "mph"
            else -> "km/h"
        }

        val speedMultiplier = when (unit) {
            sabuj.m.truedistance.database.UnitPreference.MILES -> 2.23694
            else -> 3.6
        }

        val currentSpeedVal = state.currentSpeedMps * speedMultiplier
        val avgSpeedVal = state.averageSpeedMps * speedMultiplier
        val maxSpeedVal = state.maxSpeedMps * speedMultiplier

        val formattedDistance = DistanceCalculator.format(
            state.distanceCoveredMeters, unit, precision, autoMeters
        )

        val startTimeStr = if (state.startedAtMillis > 0) {
            timeFormatter.format(Date(state.startedAtMillis))
        } else {
            "--:--:--"
        }

        val elapsedStr = formatElapsed(state.elapsedMillis)

        SpeedometerUiState(
            isTracking = state.isTracking,
            isPaused = state.isPaused,
            formattedSpeed = String.format(Locale.US, "%.1f", currentSpeedVal),
            speedUnitLabel = speedUnit,
            formattedDistance = formattedDistance,
            formattedAvgSpeed = String.format(Locale.US, "%.1f %s", avgSpeedVal, speedUnit),
            formattedMaxSpeed = String.format(Locale.US, "%.1f %s", maxSpeedVal, speedUnit),
            formattedStartTime = startTimeStr,
            formattedElapsedTime = elapsedStr,
            currentLocation = state.currentLocation,
            pathPoints = state.pathPoints,
            staleFix = state.staleFix
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SpeedometerUiState()
    )

    fun startTrip(context: Context) {
        val intent = Intent(context, SpeedometerService::class.java).apply {
            action = SpeedometerService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    fun pauseTrip(context: Context) {
        val intent = Intent(context, SpeedometerService::class.java).apply {
            action = SpeedometerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeTrip(context: Context) {
        val intent = Intent(context, SpeedometerService::class.java).apply {
            action = SpeedometerService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun stopTrip(context: Context) {
        val intent = Intent(context, SpeedometerService::class.java).apply {
            action = SpeedometerService.ACTION_STOP
        }
        context.startService(intent)
    }

    private fun formatElapsed(millis: Long): String {
        val totalSec = millis / 1000
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }
}
