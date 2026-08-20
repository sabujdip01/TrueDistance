package sabuj.m.truedistance

import android.app.Application
import android.util.Log
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrueDistanceApp : Application() {
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
    }
}
