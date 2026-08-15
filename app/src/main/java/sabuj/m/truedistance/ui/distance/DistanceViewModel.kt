package sabuj.m.truedistance.ui.distance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.repository.SavedLocationRepository
import javax.inject.Inject

/** §6.1.1 Main Screen ViewModel */
data class DestinationSelection(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val savedLocationId: String? = null
)

data class DistanceUiState(
    val savedLocations: List<SavedLocation> = emptyList(),
    val selectedDestination: DestinationSelection? = null,
    val isStartEnabled: Boolean = false
)

@HiltViewModel
class DistanceViewModel @Inject constructor(
    private val savedLocationRepository: SavedLocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DistanceUiState())
    val uiState: StateFlow<DistanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            savedLocationRepository.observeAll().collect { locations ->
                _uiState.value = _uiState.value.copy(savedLocations = locations)
            }
        }
    }

    fun selectDestination(destination: DestinationSelection) {
        _uiState.value = _uiState.value.copy(
            selectedDestination = destination,
            isStartEnabled = true
        )
    }

    fun selectSavedLocation(location: SavedLocation) {
        selectDestination(
            DestinationSelection(
                name = location.name,
                address = location.address,
                latitude = location.latitude,
                longitude = location.longitude,
                savedLocationId = location.id
            )
        )
    }

    fun clearDestination() {
        _uiState.value = _uiState.value.copy(
            selectedDestination = null,
            isStartEnabled = false
        )
    }
}
