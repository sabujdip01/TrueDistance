package sabuj.m.truedistance

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.service.SpeedometerService
import sabuj.m.truedistance.service.TrackingService
import javax.inject.Inject

/**
 * TrueDistanceApp is the custom Application subclass for the app.
 *
 * Responsibilities:
 * 1. Initializes the Google Places API SDK with the configured Maps API Key.
 * 2. Pre-applies the saved theme (Light / Dark / System) on cold app startup before any UI inflation.
 * 3. Tracks global Activity lifecycle transitions to detect when the entire application is backgrounded.
 * 4. Gracefully auto-stops TrackingService and SpeedometerService when the app is minimized if Background Tracking
 *    is disabled in Settings, while ignoring transient configuration changes (such as device rotations).
 */
@HiltAndroidApp
class TrueDistanceApp : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var runningActivities = 0
    private var isChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()

        // Initialize Google Places SDK
        try {
            if (!Places.isInitialized()) {
                Log.d("TrueDistanceApp", "Initializing Places SDK with key: ${BuildConfig.MAPS_API_KEY.take(5)}...")
                Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
            }
        } catch (e: Exception) {
            Log.e("TrueDistanceApp", "Error initializing Places SDK", e)
        }

        // Apply saved theme preference on cold startup
        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                val mode = settingsRepository.theme.first()
                val targetNightMode = when (mode) {
                    sabuj.m.truedistance.database.ThemeMode.LIGHT -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    sabuj.m.truedistance.database.ThemeMode.DARK -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    sabuj.m.truedistance.database.ThemeMode.SYSTEM -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != targetNightMode) {
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(targetNightMode)
                }
            } catch (_: Exception) {}
        }
    }
}
