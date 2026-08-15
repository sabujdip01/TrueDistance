package sabuj.m.truedistance.ui.distance

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.DistanceSnapshot
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.database.UnitPreference
import sabuj.m.truedistance.repository.HistoryRepository
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.utils.DistanceCalculator
import sabuj.m.truedistance.utils.LocationTrackingHelper
import java.util.UUID
import javax.inject.Inject

/** §6.1.4 Tracking Screen ViewModel — live distance, markers, polyline, snapshots. */
data class TrackingUiState(
    val currentLocation: LatLng? = null,
    val destination: LatLng? = null,
    val distanceMeters: Double = 0.0,
    val formattedDistance: String = "",
    val isTracking: Boolean = false,
    val staleFix: Boolean = false // §11.2 — signal-lost indicator
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext context: android.content.Context,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val locationHelper = LocationTrackingHelper(context)

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private var snapshotJob: Job? = null
    private var historyEntryId: String? = null
    private var startedAt: Long = 0L
    private var lastFixAt: Long = 0L

    /** §6.1.1/6.1.4 — begin a session for the given destination + name. */
    fun startTracking(destinationName: String, destLat: Double, destLng: Double, savedLocationId: String?) {
        viewModelScope.launch {
            val accuracyMode = settingsRepository.gpsAccuracyMode.first()
            val intervalSeconds = settingsRepository.updateFrequencySeconds.first()

            startedAt = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                destination = LatLng(destLat, destLng),
                isTracking = true
            )

            trackingJob = locationHelper.observeLocation(intervalSeconds, accuracyMode)
                .let { flow ->
                    viewModelScope.launch {
                        flow.collect { location ->
                            onLocationUpdate(location, destinationName, destLat, destLng, savedLocationId)
                        }
                    }
                }

            // §11.2 — flag stale fix if no update for 20s+
            snapshotJob = viewModelScope.launch {
                while (true) {
                    kotlinx.coroutines.delay(5_000)
                    val stale = lastFixAt != 0L && System.currentTimeMillis() - lastFixAt > 20_000
                    _uiState.value = _uiState.value.copy(staleFix = stale)
                }
            }
        }
    }

    private suspend fun onLocationUpdate(
        location: Location,
        destinationName: String,
        destLat: Double,
        destLng: Double,
        savedLocationId: String?
    ) {
        lastFixAt = System.currentTimeMillis()

        val distance = DistanceCalculator.haversineMeters(
            location.latitude, location.longitude, destLat, destLng
        )
        val unit = settingsRepository.unit.first()
        val precision = settingsRepository.decimalPrecision.first()
        val autoMeters = settingsRepository.autoMetersUnder1km.first()

        _uiState.value = _uiState.value.copy(
            currentLocation = LatLng(location.latitude, location.longitude),
            distanceMeters = distance,
            formattedDistance = DistanceCalculator.format(distance, unit, precision, autoMeters),
            staleFix = false
        )

        // §6.1.3/§8 — create the HistoryEntry on first fix, then keep updating it.
        if (historyEntryId == null) {
            val id = UUID.randomUUID().toString()
            historyEntryId = id
            historyRepository.startSession(
                HistoryEntry(
                    id = id,
                    destinationName = destinationName,
                    destinationLat = destLat,
                    destinationLng = destLng,
                    initialDistanceMeters = distance,
                    startedAt = startedAt,
                    savedLocationId = savedLocationId
                )
            )
            recordSnapshot(id, 0, distance)
        } else {
            val elapsedPercent = elapsedPercentSoFar()
            recordSnapshot(historyEntryId!!, elapsedPercent, distance)
        }
    }

    private suspend fun recordSnapshot(entryId: String, elapsedPercent: Int, distance: Double) {
        historyRepository.recordSnapshot(
            DistanceSnapshot(
                historyEntryId = entryId,
                timestamp = System.currentTimeMillis(),
                elapsedPercent = elapsedPercent,
                distanceMeters = distance
            )
        )
    }

    // Rough estimate until Stop is pressed — refined to exact 100 on stop.
    private fun elapsedPercentSoFar(): Int {
        val elapsed = System.currentTimeMillis() - startedAt
        return ((elapsed.toDouble() / (elapsed + 60_000)) * 100).toInt().coerceIn(0, 99)
    }

    /** §6.1.4 — Stop Tracking button. */
    fun stopTracking() {
        viewModelScope.launch {
            trackingJob?.cancel()
            snapshotJob?.cancel()

            val id = historyEntryId
            if (id != null) {
                val entry = historyRepository.getById(id)
                if (entry != null) {
                    historyRepository.updateSession(
                        entry.copy(
                            finalDistanceMeters = _uiState.value.distanceMeters,
                            endedAt = System.currentTimeMillis()
                        )
                    )
                }
                recordSnapshot(id, 100, _uiState.value.distanceMeters)
            }

            _uiState.value = _uiState.value.copy(isTracking = false)
            historyEntryId = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
        snapshotJob?.cancel()
    }
}
