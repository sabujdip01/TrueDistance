package sabuj.m.truedistance.database

/** §8 Data Model — AppSettings, stored via DataStore (see SettingsRepository) */
object AppSettingsKeys {
    const val THEME = "theme"
    const val UNIT = "unit"
    const val DECIMAL_PRECISION = "decimal_precision"
    const val AUTO_METERS_UNDER_1KM = "auto_meters_under_1km"
    const val GPS_ACCURACY_MODE = "gps_accuracy_mode"
    const val UPDATE_FREQUENCY_SECONDS = "update_frequency_seconds"
    const val BACKGROUND_TRACKING_ENABLED = "background_tracking_enabled"
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class UnitPreference { KM, MILES, BOTH }
enum class GpsAccuracyMode { HIGH, BALANCED, DEVICE_ONLY }
