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

    /** Flag set by TrackingFragment on stop to tell DistanceFragment to clear the selection. */
    private var _shouldClearDestination = false

    fun setDestination(destination: DestinationSelection) {
        _destination.value = destination
    }

    fun clear() {
        _destination.value = null
    }

    /** Called by TrackingFragment when tracking is stopped — clears destination too. */
    fun requestClearDestination() {
        _shouldClearDestination = true
        _destination.value = null
    }

    /** Called by DistanceFragment in onResume — returns true once, then resets. */
    fun consumeClearRequest(): Boolean {
        val was = _shouldClearDestination
        _shouldClearDestination = false
        return was
    }
}
