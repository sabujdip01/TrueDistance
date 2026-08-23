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

@HiltAndroidApp
class TrueDistanceApp : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var runningActivities = 0
    private var isChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        try {
            if (!Places.isInitialized()) {
                Log.d("TrueDistanceApp", "Initializing Places SDK with key: ${BuildConfig.MAPS_API_KEY.take(5)}...")
                Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
            }
        } catch (e: Exception) {
            Log.e("TrueDistanceApp", "Error initializing Places SDK", e)
        }

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

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                if (++runningActivities == 1 && !isChangingConfigurations) {
                    // App brought to foreground
                }
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                isChangingConfigurations = activity.isChangingConfigurations
                if (--runningActivities == 0 && !isChangingConfigurations) {
                    // Entire application sent to background (Home button / App switch)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val bgEnabled = settingsRepository.backgroundTrackingEnabled.first()
                            if (!bgEnabled) {
                                // Stop True Distance tracking service
                                val trackingStopIntent = Intent(applicationContext, TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_STOP
                                }
                                startService(trackingStopIntent)

                                // Stop Speedometer service
                                val speedometerStopIntent = Intent(applicationContext, SpeedometerService::class.java).apply {
                                    action = SpeedometerService.ACTION_STOP
                                }
                                startService(speedometerStopIntent)
                            }
                        } catch (e: Exception) {
                            Log.e("TrueDistanceApp", "Error stopping services on app background", e)
                        }
                    }
                }
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
