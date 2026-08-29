package sabuj.m.truedistance.ui.savedlocations

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.repository.SavedLocationRepository
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * §6.1.2 Saved Locations Screen ViewModel — Manages list of bookmarked destinations,
 * geocoding resolution for street addresses, and deletion operations.
 */
@HiltViewModel
class SavedLocationsViewModel @Inject constructor(
    private val repository: SavedLocationRepository
) : ViewModel() {

    /** StateFlow emitting the list of all saved locations. */
    val savedLocations: StateFlow<List<SavedLocation>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Adds a newly bookmarked location directly with coordinates.
     */
    fun addLocation(name: String, address: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.save(
                SavedLocation(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    address = address,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    /**
     * §6.1.2 — Adds a location by searching and geocoding an address query.
     */
    fun addFromAddress(query: String, context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault()).getFromLocationName(query, 1)
                }.getOrNull()?.firstOrNull()
            }
            if (result != null) {
                addLocation(
                    name = result.featureName ?: query,
                    address = result.getAddressLine(0) ?: query,
                    latitude = result.latitude,
                    longitude = result.longitude
                )
            }
            onResult(result != null)
        }
    }

    /**
     * §6.1.2 — Adds a location from a map coordinate, reverse-geocoding to resolve street address.
     */
    fun addFromPoint(latitude: Double, longitude: Double, name: String, context: Context) {
        viewModelScope.launch {
            val address = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
                }.getOrNull()?.firstOrNull()
            }
            addLocation(
                name = name.ifBlank { address?.featureName ?: "Pinned location" },
                address = address?.getAddressLine(0) ?: "$latitude, $longitude",
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    /**
     * Deletes a saved bookmark from the repository.
     */
    fun delete(location: SavedLocation) {
        viewModelScope.launch {
            repository.delete(location)
        }
    }
}
