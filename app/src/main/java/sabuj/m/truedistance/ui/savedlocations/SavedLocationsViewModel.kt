package sabuj.m.truedistance.ui.savedlocations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.repository.SavedLocationRepository
import java.util.UUID
import javax.inject.Inject

/** §6.1.2 Saved Locations Screen ViewModel */
@HiltViewModel
class SavedLocationsViewModel @Inject constructor(
    private val repository: SavedLocationRepository
) : ViewModel() {

    val savedLocations: StateFlow<List<SavedLocation>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Called after either search-autocomplete or map-pick resolves a lat/lng + name. */
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

    fun delete(location: SavedLocation) {
        viewModelScope.launch {
            repository.delete(location)
        }
    }
}
