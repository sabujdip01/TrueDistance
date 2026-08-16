package sabuj.m.truedistance.ui.mappicker

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * §6.1.1b / §6.1.2 — shared picked-point holder for the "pick on map" flow, used by
 * both Main Screen destination picking and Saved Locations "add via map" flow.
 * Activity-scoped so the picker Fragment and its caller can exchange a result
 * without Safe Args (LatLng needs a wrapper to be nav-arg-friendly anyway).
 */
@HiltViewModel
class MapPickerViewModel @Inject constructor() : ViewModel() {
    private val _pickedPoint = MutableStateFlow<LatLng?>(null)
    val pickedPoint: StateFlow<LatLng?> = _pickedPoint.asStateFlow()

    fun setPickedPoint(point: LatLng) {
        _pickedPoint.value = point
    }

    fun consume(): LatLng? {
        val point = _pickedPoint.value
        _pickedPoint.value = null
        return point
    }
}
