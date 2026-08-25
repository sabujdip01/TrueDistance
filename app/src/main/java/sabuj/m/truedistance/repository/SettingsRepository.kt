package sabuj.m.truedistance.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sabuj.m.truedistance.database.GpsAccuracyMode
import sabuj.m.truedistance.database.ThemeMode
import sabuj.m.truedistance.database.UnitPreference
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_settings")

/**
 * §8 Data Model / §6.3 Settings — Repository managing user preferences via Jetpack Preferences DataStore.
 *
 * Persists:
 * - Appearance Theme (System, Light, Dark)
 * - Measurement Units (KM / Miles)
 * - Decimal precision
 * - Auto meters switch for short distances (<1km)
 * - GPS accuracy profile (High, Balanced, Device Only)
 * - Polling update frequency (1s, 3s, 5s, 10s)
 * - Background tracking enable toggle
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val UNIT = stringPreferencesKey("unit")
        val DECIMAL_PRECISION = intPreferencesKey("decimal_precision")
        val AUTO_METERS_UNDER_1KM = booleanPreferencesKey("auto_meters_under_1km")
        val GPS_ACCURACY_MODE = stringPreferencesKey("gps_accuracy_mode")
        val UPDATE_FREQUENCY_SECONDS = intPreferencesKey("update_frequency_seconds")
        val BACKGROUND_TRACKING_ENABLED = booleanPreferencesKey("background_tracking_enabled")
    }

    /** Observes theme mode preference. */
    val theme: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.valueOf(it[Keys.THEME] ?: ThemeMode.SYSTEM.name)
    }

    /** Observes measurement unit preference (KM vs Miles). */
    val unit: Flow<UnitPreference> = context.dataStore.data.map {
        UnitPreference.valueOf(it[Keys.UNIT] ?: UnitPreference.KM.name)
    }

    /** Observes decimal precision (0, 1, or 2 decimal places). */
    val decimalPrecision: Flow<Int> = context.dataStore.data.map {
        it[Keys.DECIMAL_PRECISION] ?: 2
    }

    /** Observes whether distances below 1km are auto-switched to meters/feet. */
    val autoMetersUnder1km: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.AUTO_METERS_UNDER_1KM] ?: true
    }

    /** Observes GPS hardware accuracy mode. */
    val gpsAccuracyMode: Flow<GpsAccuracyMode> = context.dataStore.data.map {
        GpsAccuracyMode.valueOf(it[Keys.GPS_ACCURACY_MODE] ?: GpsAccuracyMode.HIGH.name)
    }

    /** Observes GPS polling update interval in seconds. */
    val updateFrequencySeconds: Flow<Int> = context.dataStore.data.map {
        it[Keys.UPDATE_FREQUENCY_SECONDS] ?: 3
    }

    /** Observes background tracking permission toggle. */
    val backgroundTrackingEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.BACKGROUND_TRACKING_ENABLED] ?: true
    }

    /** Updates theme preference. */
    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    /** Updates measurement unit preference. */
    suspend fun setUnit(unit: UnitPreference) {
        context.dataStore.edit { it[Keys.UNIT] = unit.name }
    }

    /** Updates decimal precision. */
    suspend fun setDecimalPrecision(value: Int) {
        context.dataStore.edit { it[Keys.DECIMAL_PRECISION] = value.coerceIn(0, 2) }
    }

    /** Updates auto meters switch state. */
    suspend fun setAutoMetersUnder1km(value: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_METERS_UNDER_1KM] = value }
    }

    /** Updates GPS accuracy mode. */
    suspend fun setGpsAccuracyMode(mode: GpsAccuracyMode) {
        context.dataStore.edit { it[Keys.GPS_ACCURACY_MODE] = mode.name }
    }

    /** Updates GPS update interval in seconds. */
    suspend fun setUpdateFrequencySeconds(value: Int) {
        context.dataStore.edit { it[Keys.UPDATE_FREQUENCY_SECONDS] = value }
    }

    /** Updates background tracking toggle. */
    suspend fun setBackgroundTrackingEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.BACKGROUND_TRACKING_ENABLED] = value }
    }
}
