package sabuj.m.truedistance.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sabuj.m.truedistance.BuildConfig
import sabuj.m.truedistance.MainActivity
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.ActivitySplashBinding
import sabuj.m.truedistance.utils.GpsStatusHelper

/**
 * SplashActivity serves as the branded launch screen and hardware requirement gateway.
 *
 * Responsibilities:
 * 1. Displays custom branded UI (app logo, name, and semantic version number) for a minimum of 1.5 seconds.
 * 2. Enforces device Location Services availability before transitioning into MainActivity.
 * 3. Listens in real time via LocationManager.PROVIDERS_CHANGED_ACTION, window focus changes, and
 *    coroutine watchers so that toggling Location from Quick Settings / Status Bar immediately
 *    dismisses the prompt dialog and enters the main application seamlessly.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var splashDone = false
    private var locationDialog: AlertDialog? = null

    /**
     * BroadcastReceiver listening for system-wide location provider status changes.
     * Fires immediately when the user toggles Location in Android Quick Settings.
     */
    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                if (splashDone) {
                    proceedIfLocationEnabled()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Dismiss the system splash screen immediately so our branded layout displays directly
        splashScreen.setKeepOnScreenCondition { false }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system navigation bar window insets so footer text never overlaps 3-button nav bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, 0, 0, systemBarInsets.bottom)
            insets
        }

        // Render current semantic version code & name from BuildConfig
        binding.versionText.text = getString(R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        lifecycleScope.launch {
            // Keep branded splash visible for 1.5 seconds before validating location
            delay(1500)
            splashDone = true
            proceedIfLocationEnabled()

            // Active polling watcher to guarantee immediate entry if toggled via status bar
            while (!isFinishing) {
                delay(800)
                if (GpsStatusHelper.isLocationEnabled(this@SplashActivity)) {
                    proceedIfLocationEnabled()
                    break
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Register receiver for real-time location provider broadcasts
        try {
            registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        } catch (_: Exception) {}
    }

    override fun onStop() {
        super.onStop()
        // Unregister receiver to prevent lifecycle leaks
        try {
            unregisterReceiver(gpsReceiver)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        // Re-check location state when returning from system settings
        if (splashDone) {
            proceedIfLocationEnabled()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Detect when the notification shade is dismissed after toggling the Location tile
        if (hasFocus && splashDone) {
            proceedIfLocationEnabled()
        }
    }

    /**
     * Checks if Location Services are active. If active, dismisses any open dialog
     * and proceeds to MainActivity with a fade transition. Otherwise, presents the enforcement dialog.
     */
    private fun proceedIfLocationEnabled() {
        if (GpsStatusHelper.isLocationEnabled(this)) {
            locationDialog?.dismiss()
            locationDialog = null
            startActivity(Intent(this, MainActivity::class.java))
            // Smooth fade transition between splash and main screen
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } else if (locationDialog == null || !locationDialog!!.isShowing) {
            showLocationRequiredDialog()
        }
    }

    /**
     * Displays a non-cancelable Material alert dialog instructing the user to enable Location Services.
     */
    private fun showLocationRequiredDialog() {
        locationDialog?.dismiss()
        locationDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Location Required")
            .setMessage("True Distance needs location services to work. Please enable location to continue.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                locationDialog = null
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .show()
    }
}
