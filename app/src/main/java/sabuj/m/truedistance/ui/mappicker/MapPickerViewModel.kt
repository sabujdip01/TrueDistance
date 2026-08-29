package sabuj.m.truedistance.ui.mappicker

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * §6.1.1b / §6.1.2 — Activity-scoped ViewModel holding coordinates selected from the interactive Map Picker.
 */
@HiltViewModel
class MapPickerViewModel @Inject constructor() : ViewModel() {
    private val _pickedPoint = MutableStateFlow<LatLng?>(null)
    val pickedPoint: StateFlow<LatLng?> = _pickedPoint.asStateFlow()

    /** Stores the user-selected pinpoint coordinate. */
    fun setPickedPoint(point: LatLng) {
        _pickedPoint.value = point
    }

    /** Consumes and clears the picked coordinate exactly once. */
    fun consume(): LatLng? {
        val point = _pickedPoint.value
        _pickedPoint.value = null
        return point
    }
}
