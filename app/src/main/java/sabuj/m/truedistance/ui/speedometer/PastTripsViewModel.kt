package sabuj.m.truedistance.ui.speedometer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.Trip
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.repository.TripRepository
import javax.inject.Inject

data class PastTripListItem(
    val trip: Trip,
    val isExpanded: Boolean
)

data class PastTripsUiState(
    val items: List<PastTripListItem> = emptyList(),
    val unit: sabuj.m.truedistance.database.UnitPreference = sabuj.m.truedistance.database.UnitPreference.KM,
    val decimalPrecision: Int = 2,
    val autoMetersUnder1km: Boolean = true
)

@HiltViewModel
class PastTripsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val expandedTripId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PastTripsUiState> = combine(
        tripRepository.observeAll(),
        expandedTripId,
        settingsRepository.unit,
        settingsRepository.decimalPrecision,
        settingsRepository.autoMetersUnder1km
    ) { trips, expandedId, unit, precision, autoMeters ->
        val items = trips.map { trip ->
            PastTripListItem(
                trip = trip,
                isExpanded = trip.id == expandedId
            )
        }
        PastTripsUiState(
            items = items,
            unit = unit,
            decimalPrecision = precision,
            autoMetersUnder1km = autoMeters
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PastTripsUiState()
    )

    fun toggleExpand(trip: Trip) {
        expandedTripId.value = if (expandedTripId.value == trip.id) null else trip.id
    }

    fun delete(trip: Trip) {
        viewModelScope.launch {
            if (expandedTripId.value == trip.id) {
                expandedTripId.value = null
            }
            tripRepository.delete(trip)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            expandedTripId.value = null
            tripRepository.deleteAll()
        }
    }
}
