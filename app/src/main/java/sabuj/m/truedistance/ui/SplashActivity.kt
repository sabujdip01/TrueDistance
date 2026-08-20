package sabuj.m.truedistance.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var keepSplash = true
    private var splashDone = false
    private var dialogShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen BEFORE super.onCreate() so the
        // Theme.SplashScreen → AppCompat theme transition happens correctly.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the system splash on screen for the initial delay
        splashScreen.setKeepOnScreenCondition { keepSplash }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show version number
        binding.versionText.text = getString(R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        lifecycleScope.launch {
            // Hold the system splash for 1.5 seconds
            delay(1500)
            keepSplash = false

            // Show our custom splash layout (with version) for another 1.5 seconds
            delay(1500)
            splashDone = true
            proceedIfLocationEnabled()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check after user returns from location settings
        if (splashDone) {
            proceedIfLocationEnabled()
        }
    }

    private fun proceedIfLocationEnabled() {
        if (GpsStatusHelper.isLocationEnabled(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            // Smooth fade transition from splash → main
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } else if (!dialogShowing) {
            showLocationRequiredDialog()
        }
    }

    private fun showLocationRequiredDialog() {
        dialogShowing = true
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Required")
            .setMessage("True Distance needs location services to work. Please enable location to continue.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                dialogShowing = false
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .show()
    }
}
