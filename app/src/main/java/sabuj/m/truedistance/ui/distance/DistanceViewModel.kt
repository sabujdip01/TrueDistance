package sabuj.m.truedistance.ui.distance

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.repository.SavedLocationRepository
import java.util.Locale
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
    val isStartEnabled: Boolean = false,
    val isSearching: Boolean = false,
    val searchError: String? = null
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
            isStartEnabled = true,
            searchError = null
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

    /**
     * §6.1.1a — resolves a free-text query to a destination via Android's built-in
     * Geocoder (interim implementation ahead of Places Autocomplete, §6.1.1a note
     * in DistanceFragment).
     */
    fun searchByAddress(query: String, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault()).getFromLocationName(query, 1)
                }.getOrNull()?.firstOrNull()
            }

            if (result != null) {
                selectDestination(
                    DestinationSelection(
                        name = result.featureName ?: query,
                        address = result.getAddressLine(0) ?: query,
                        latitude = result.latitude,
                        longitude = result.longitude
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(searchError = "No results found for \"$query\"")
            }
            _uiState.value = _uiState.value.copy(isSearching = false)
        }
    }

    /** §6.1.1b — resolves a map-tapped point (reverse-geocode for a display name). */
    fun selectPickedPoint(latitude: Double, longitude: Double, context: Context) {
        viewModelScope.launch {
            val address = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
                }.getOrNull()?.firstOrNull()
            }

            selectDestination(
                DestinationSelection(
                    name = address?.featureName ?: "Pinned location",
                    address = address?.getAddressLine(0) ?: "$latitude, $longitude",
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun clearDestination() {
        _uiState.value = _uiState.value.copy(
            selectedDestination = null,
            isStartEnabled = false
        )
    }
}
