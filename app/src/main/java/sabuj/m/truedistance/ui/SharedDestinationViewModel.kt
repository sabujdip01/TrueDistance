package sabuj.m.truedistance.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sabuj.m.truedistance.ui.distance.DestinationSelection
import javax.inject.Inject

/**
 * Activity-scoped shared ViewModel — communicates selected destinations and reset signals
 * between DistanceFragment (§6.1.1), SavedLocationsFragment (§6.1.2), and TrackingFragment (§6.1.4).
 */
@HiltViewModel
class SharedDestinationViewModel @Inject constructor() : ViewModel() {
    private val _destination = MutableStateFlow<DestinationSelection?>(null)
    val destination: StateFlow<DestinationSelection?> = _destination.asStateFlow()

    /** Flag set by TrackingFragment on stop to tell DistanceFragment to clear the search bar. */
    private var _shouldClearDestination = false

    /** Sets the current active destination selection. */
    fun setDestination(destination: DestinationSelection) {
        _destination.value = destination
    }

    /** Clears the current destination selection in memory. */
    fun clear() {
        _destination.value = null
    }

    /** Called by TrackingFragment when tracking finishes to request DistanceFragment reset its search box. */
    fun requestClearDestination() {
        _shouldClearDestination = true
        _destination.value = null
    }

    /** Called by DistanceFragment in onResume to consume the clear request exactly once. */
    fun consumeClearRequest(): Boolean {
        val was = _shouldClearDestination
        _shouldClearDestination = false
        return was
    }
}
