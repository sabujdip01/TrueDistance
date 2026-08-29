package sabuj.m.truedistance.database

/**
 * §8 Data Model — Preferences DataStore keys and domain enumerations for True Distance.
 */
object AppSettingsKeys {
    const val THEME = "theme"
    const val UNIT = "unit"
    const val DECIMAL_PRECISION = "decimal_precision"
    const val AUTO_METERS_UNDER_1KM = "auto_meters_under_1km"
    const val GPS_ACCURACY_MODE = "gps_accuracy_mode"
    const val UPDATE_FREQUENCY_SECONDS = "update_frequency_seconds"
    const val KEEP_SCREEN_ON = "keep_screen_on"
}

/** Supported application theme appearance modes. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Measurement unit preference for distances and speeds. */
enum class UnitPreference { KM, MILES, BOTH }

/** Hardware GPS positioning accuracy configurations. */
enum class GpsAccuracyMode { HIGH, BALANCED, DEVICE_ONLY }
