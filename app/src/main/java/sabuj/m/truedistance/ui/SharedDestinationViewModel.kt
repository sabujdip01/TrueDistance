package sabuj.m.truedistance.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sabuj.m.truedistance.ui.distance.DestinationSelection
import javax.inject.Inject

/**
 * Activity-scoped shared ViewModel — passes the chosen destination from
 * DistanceFragment (§6.1.1) to TrackingFragment (§6.1.4) without Safe Args,
 * since the destination object isn't Parcelable-simple (comes from search/map/
 * saved-location, each with different source data).
 */
@HiltViewModel
class SharedDestinationViewModel @Inject constructor() : ViewModel() {
    private val _destination = MutableStateFlow<DestinationSelection?>(null)
    val destination: StateFlow<DestinationSelection?> = _destination.asStateFlow()

    fun setDestination(destination: DestinationSelection) {
        _destination.value = destination
    }

    fun clear() {
        _destination.value = null
    }
}
