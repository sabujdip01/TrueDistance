package sabuj.m.truedistance

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

/** §4 App Navigation Structure, §5.2 Splash Screen */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // §10 Permissions — POST_NOTIFICATIONS needed for background tracking notification
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op either way; background tracking still works, notification just won't show */ }

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

        // When switching tabs, always navigate cleanly to the selected tab's root.
        // We do NOT use saveState/restoreState — that caused the tab to restore
        // a previous sub-page (e.g. Saved Locations) instead of landing on the root.
        bottomNav.setOnItemSelectedListener { item ->
            val rootId = tabRoots[item.itemId] ?: return@setOnItemSelectedListener false
            navController.navigate(
                rootId,
                null,
                NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    // Pop everything back to the graph start before navigating,
                    // ensuring no stale sub-destinations remain on the back stack.
                    .setPopUpTo(navController.graph.startDestinationId, inclusive = false)
                    .build()
            )
            true
        }

        if (intent?.getStringExtra("NAVIGATE_TO") == "speedometer") {
            bottomNav.selectedItemId = R.id.nav_speedometer
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.getStringExtra("NAVIGATE_TO") == "speedometer") {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
            bottomNav.selectedItemId = R.id.nav_speedometer
        }
    }
}
