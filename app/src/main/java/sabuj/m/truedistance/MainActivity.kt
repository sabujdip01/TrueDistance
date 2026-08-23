package sabuj.m.truedistance

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

/** §4 App Navigation Structure, §5.2 Splash Screen */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // §10 Permissions — POST_NOTIFICATIONS needed for background tracking notification
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op either way; background tracking still works, notification just won't show */ }

    @javax.inject.Inject
    lateinit var settingsRepository: sabuj.m.truedistance.repository.SettingsRepository

    @javax.inject.Inject
    lateinit var trackingStateHolder: sabuj.m.truedistance.service.TrackingStateHolder

    // Tab root destinations — maps each bottom nav item to its root fragment
    private val tabRoots = mapOf(
        R.id.nav_distance   to R.id.nav_distance,
        R.id.nav_speedometer to R.id.nav_speedometer,
        R.id.nav_settings   to R.id.nav_settings
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        // Pop back to the tab's root destination when the already-selected tab is tapped again
        bottomNav.setOnItemReselectedListener { item ->
            tabRoots[item.itemId]?.let { rootId ->
                navController.popBackStack(rootId, inclusive = false)
            }
        }

        // When switching tabs, navigate cleanly to the selected tab's root.
        bottomNav.setOnItemSelectedListener { item ->
            val rootId = tabRoots[item.itemId] ?: return@setOnItemSelectedListener false
            val targetDestination = if (item.itemId == R.id.nav_distance && trackingStateHolder.state.value.isTracking) {
                R.id.nav_tracking
            } else {
                rootId
            }

            navController.navigate(
                targetDestination,
                null,
                NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(navController.graph.startDestinationId, inclusive = false)
                    .build()
            )
            true
        }

        if (savedInstanceState != null) {
            val savedTab = savedInstanceState.getInt("KEY_SELECTED_TAB", -1)
            if (savedTab != -1 && savedTab != bottomNav.selectedItemId) {
                bottomNav.selectedItemId = savedTab
            }
        } else {
            handleNavigationIntent(intent, bottomNav)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        if (bottomNav != null) {
            outState.putInt("KEY_SELECTED_TAB", bottomNav.selectedItemId)
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        handleNavigationIntent(intent, bottomNav)
    }

    private fun handleNavigationIntent(intent: android.content.Intent?, bottomNav: BottomNavigationView) {
        when (intent?.getStringExtra("NAVIGATE_TO")) {
            "speedometer" -> bottomNav.selectedItemId = R.id.nav_speedometer
            "tracking" -> bottomNav.selectedItemId = R.id.nav_distance
        }
    }
}
