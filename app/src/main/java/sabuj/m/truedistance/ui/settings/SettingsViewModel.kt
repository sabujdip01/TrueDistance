package sabuj.m.truedistance.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.GpsAccuracyMode
import sabuj.m.truedistance.database.ThemeMode
import sabuj.m.truedistance.database.UnitPreference
import sabuj.m.truedistance.repository.SettingsRepository
import javax.inject.Inject

/**
 * §6.3.1 Settings UI State — Encapsulates reactive values for all configurable user preferences.
 */
data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val unit: UnitPreference = UnitPreference.KM,
    val decimalPrecision: Int = 2,
    val autoMetersUnder1km: Boolean = true,
    val gpsAccuracyMode: GpsAccuracyMode = GpsAccuracyMode.HIGH,
    val updateFrequencySeconds: Int = 3,
    val backgroundTrackingEnabled: Boolean = true
)

/**
 * §6.3.1 Settings ViewModel — Exposes reactive [SettingsUiState] and methods to persist preference updates.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    /** Combines separate DataStore preference flows into a unified [SettingsUiState] StateFlow. */
    val uiState = combine(
        combine(repository.theme, repository.unit, repository.decimalPrecision) { theme, unit, precision ->
            Triple(theme, unit, precision)
        },
        combine(repository.autoMetersUnder1km, repository.gpsAccuracyMode, repository.updateFrequencySeconds) { auto, accuracy, freq ->
            Triple(auto, accuracy, freq)
        },
        repository.backgroundTrackingEnabled
    ) { (theme, unit, precision), (auto, accuracy, freq), backgroundEnabled ->
        SettingsUiState(
            theme = theme,
            unit = unit,
            decimalPrecision = precision,
            autoMetersUnder1km = auto,
            gpsAccuracyMode = accuracy,
            updateFrequencySeconds = freq,
            backgroundTrackingEnabled = backgroundEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Sets app theme mode (Light, Dark, System). */
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repository.setTheme(mode) }

    /** Sets measurement unit (KM, Miles). */
    fun setUnit(unit: UnitPreference) = viewModelScope.launch { repository.setUnit(unit) }

    /** Sets decimal precision (0, 1, 2). */
    fun setDecimalPrecision(value: Int) = viewModelScope.launch { repository.setDecimalPrecision(value) }

    /** Sets auto meters toggle for distances <1km. */
    fun setAutoMetersUnder1km(value: Boolean) = viewModelScope.launch { repository.setAutoMetersUnder1km(value) }

    /** Sets hardware GPS accuracy mode. */
    fun setGpsAccuracyMode(mode: GpsAccuracyMode) = viewModelScope.launch { repository.setGpsAccuracyMode(mode) }

    /** Sets GPS polling frequency in seconds. */
    fun setUpdateFrequencySeconds(value: Int) = viewModelScope.launch { repository.setUpdateFrequencySeconds(value) }

    /** Sets background tracking toggle state. */
    fun setBackgroundTrackingEnabled(value: Boolean) = viewModelScope.launch { repository.setBackgroundTrackingEnabled(value) }
}
