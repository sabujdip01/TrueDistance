package sabuj.m.truedistance.ui.distance

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentTrackingBinding
import sabuj.m.truedistance.service.TrackingState
import sabuj.m.truedistance.ui.SharedDestinationViewModel
import sabuj.m.truedistance.utils.LocationPermissionHelper

/**
 * §6.1.4 Tracking Screen — displays a live Google Map with:
 *  - Current location marker (azure/blue)
 *  - Destination marker (red)
 *  - Polyline connecting the two
 *  - Distance overlay card at the top
 *  - Stop Tracking button at the bottom
 *
 * Map Controls (all placed in the bottom-right corner):
 *  - +/- zoom buttons (native Maps SDK, enabled via uiSettings)
 *  - My Location button (native Maps SDK, repositioned from top-right to bottom-right
 *    so all controls are in a single cluster for easy one-thumb access)
 *
 * Permission flow:
 *  - Fine location + POST_NOTIFICATIONS (Android 13+) are requested on entry.
 *  - If denied, the fragment pops back immediately.
 */
@AndroidEntryPoint
class TrackingFragment : Fragment(), com.google.android.gms.maps.OnMapReadyCallback {

    // --- View Binding ---
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    /**
     * Activity-scoped ViewModel that carries the chosen destination from
     * DistanceFragment (§6.1.1) through to this screen. Accessed read-only here.
     */
    private val sharedDestinationViewModel: SharedDestinationViewModel by activityViewModels()

    /** Fragment-scoped ViewModel holding tracking state (distance, location, etc.) */
    private val viewModel: TrackingViewModel by viewModels()

    // --- Map state ---
    private var googleMap: GoogleMap? = null
    private var currentMarker: com.google.android.gms.maps.model.Marker? = null
    private var destinationMarker: com.google.android.gms.maps.model.Marker? = null
    private var polyline: com.google.android.gms.maps.model.Polyline? = null

    /** Prevents showing the "Destination Reached" dialog more than once per session. */
    private var reachedDialogShown = false

    // -----------------------------------------------------------------------------------------
    // Permission Launcher
    // -----------------------------------------------------------------------------------------

    /**
     * §10 Permissions — requests foreground location and (on API 33+) notification permission.
     * If fine location is granted, tracking begins. If denied, pop back — can't track.
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            beginTracking()
        } else {
            // §11.4 — tracking is impossible without location permission, return to main screen
            findNavController().popBackStack()
        }
    }

    // -----------------------------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isFollowLocationMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise the map asynchronously — onMapReady() is called when ready
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Stop Tracking button — stops the service and returns to the main screen
        binding.stopButton.setOnClickListener {
            viewModel.stopTracking()
            isFollowLocationMode = false
            binding.btnBackToOverview.visibility = View.GONE
            sharedDestinationViewModel.requestClearDestination()
            android.widget.Toast.makeText(requireContext(), getString(R.string.trip_saved), android.widget.Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        // Setup custom map control buttons
        binding.btnZoomIn.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        // Recenter / Focus on User Current Location (WhatsApp feature)
        binding.btnMyLocation.setOnClickListener {
            val loc = viewModel.uiState.value.currentLocation
            if (loc != null && googleMap != null) {
                if (viewModel.uiState.value.isTracking) {
                    isFollowLocationMode = true
                    binding.btnBackToOverview.visibility = View.VISIBLE
                }
                val density = resources.displayMetrics.density
                googleMap?.setPadding(0, (120 * density).toInt(), 0, (20 * density).toInt())
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17.5f))
            }
        }

        // Return to Overview / Fit Both Markers & Line (WhatsApp feature)
        binding.btnBackToOverview.setOnClickListener {
            isFollowLocationMode = false
            binding.btnBackToOverview.visibility = View.GONE
            fitOverviewBounds(viewModel.uiState.value)
        }

        // Observe tracking state: update distance text, stale indicator, and map
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update the distance overlay card at the top of the map
                    binding.distanceText.text = state.formattedDistance

                    // Show "Signal Lost" warning if GPS fix is stale
                    binding.staleIndicator.visibility =
                        if (state.staleFix) View.VISIBLE else View.GONE

                    // Tracking stopped externally (e.g. notification Stop button) — go back
                    if (!state.isTracking && currentMarker != null && !state.destinationReached) {
                        sharedDestinationViewModel.requestClearDestination()
                        context?.let { ctx ->
                            android.widget.Toast.makeText(ctx, getString(R.string.trip_saved), android.widget.Toast.LENGTH_SHORT).show()
                        }
                        findNavController().popBackStack()
                        return@collect
                    }

                    // Refresh map markers and polyline
                    updateMap(state)

                    // Show "Destination Reached" celebration dialog
                    if (state.destinationReached && !reachedDialogShown) {
                        reachedDialogShown = true
                        showDestinationReachedDialog(state.destinationName)
                    }
                }
            }
        }

        // Request permissions and start tracking if already granted
        requestPermissionsAndStart()
    }

    // -----------------------------------------------------------------------------------------
    // Permission & Tracking Start
    // -----------------------------------------------------------------------------------------

    /**
     * Checks for fine location permission and either starts tracking immediately
     * or launches the permission request dialog.
     */
    private fun requestPermissionsAndStart() {
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            beginTracking()
        } else {
            val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            // POST_NOTIFICATIONS required for the foreground service notification on API 33+
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                perms += Manifest.permission.POST_NOTIFICATIONS
            }
            permissionLauncher.launch(perms.toTypedArray())
        }
    }

    /**
     * Starts a new tracking session for the destination held in [SharedDestinationViewModel].
     * Guards against re-starting if tracking is already in progress (e.g., returning to this
     * screen while the foreground service is still running).
     */
    private fun beginTracking() {
        if (viewModel.uiState.value.isTracking) return  // already tracking — no-op

        sharedDestinationViewModel.destination.value?.let { destination ->
            viewModel.startTracking(
                destination.name,
                destination.latitude,
                destination.longitude,
                destination.savedLocationId
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // Map Setup
    // -----------------------------------------------------------------------------------------

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Disable native zoom controls & locate button in favor of our unified custom stack
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false

        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            try {
                map.isMyLocationEnabled = true
            } catch (_: SecurityException) {
                // Permission revoked between check and map setup — safe to ignore
            }
        }

        // Tell the Maps SDK about UI overlays so bounds/camera calculations avoid them.
        val density = resources.displayMetrics.density
        val topPadding = (140 * density).toInt()    // ~140dp for distance card + margin
        val bottomPadding = (100 * density).toInt() // ~100dp for bottom stop tracking button
        val rightPadding = (64 * density).toInt()   // ~64dp for right-side 3-button map control stack
        map.setPadding(32, topPadding, rightPadding, bottomPadding)

        // Draw markers and polyline using the latest known tracking state
        updateMap(viewModel.uiState.value)
    }

    // -----------------------------------------------------------------------------------------
    // Map Updates (called on every state emission)
    // -----------------------------------------------------------------------------------------

    /**
     * Updates the map to reflect [state]:
     *  - Adds/moves the current-location marker (blue)
     *  - Adds the destination marker (green) on first fix
     *  - Redraws the polyline connecting the two points
     */
    private fun updateMap(state: TrackingState) {
        val map = googleMap ?: return

        // --- Current location marker ---
        if (state.currentLocation != null) {
            if (currentMarker == null) {
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(state.currentLocation)
                        .title(getString(R.string.you))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
            } else {
                currentMarker?.position = state.currentLocation
            }
        }

        // --- Destination marker (added once, never moved) ---
        if (state.destination != null && destinationMarker == null) {
            destinationMarker = map.addMarker(
                MarkerOptions()
                    .position(state.destination)
                    .title(getString(R.string.destination))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }

        // --- Polyline from current → destination ---
        if (state.currentLocation != null && state.destination != null) {
            polyline?.remove()
            polyline = map.addPolyline(
                PolylineOptions()
                    .add(state.currentLocation, state.destination)
                    .color(0xFF00796B.toInt())  // Dark teal — visible against all map styles
                    .width(8f)
            )
        }

        // --- Auto-fit camera or follow current location ---
        if (state.isTracking) {
            if (isFollowLocationMode) {
                state.currentLocation?.let {
                    map.animateCamera(CameraUpdateFactory.newLatLng(it))
                }
            } else {
                fitOverviewBounds(state)
            }
        }
    }

    /**
     * WhatsApp-style overview bounds calculation:
     * Fits both markers (current location & destination) and straight polyline path
     * with comfortable screen padding to avoid overlay obstructions.
     */
    private fun fitOverviewBounds(state: TrackingState) {
        val map = googleMap ?: return
        val density = resources.displayMetrics.density

        val topPadding = (140 * density).toInt()    // ~140dp for distance card + margin
        val bottomPadding = (100 * density).toInt() // ~100dp for bottom stop tracking button
        val rightPadding = (85 * density).toInt()   // ~85dp for right-side floating controls stack
        val leftPadding = (40 * density).toInt()
        map.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)

        if (state.currentLocation != null && state.destination != null) {
            val bounds = LatLngBounds.builder()
                .include(state.currentLocation)
                .include(state.destination)
                .build()
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (30 * density).toInt()))
            } catch (_: Exception) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(state.currentLocation, 14f))
            }
        } else if (state.currentLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(state.currentLocation, 16f))
        }
    }

    // -----------------------------------------------------------------------------------------
    // Destination Reached Dialog
    // -----------------------------------------------------------------------------------------

    /**
     * Shows an animated celebration dialog when the user arrives within ~10m of their
     * destination. The dialog uses a scale + fade entrance animation with an overshoot
     * bounce. Tapping "Close" returns to the main screen.
     */
    private fun showDestinationReachedDialog(destinationName: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_destination_reached, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Transparent background so the rounded corners of our custom layout show through
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set the destination name text
        val nameText = dialogView.findViewById<android.widget.TextView>(R.id.reachedDestinationName)
        nameText.text = if (destinationName.isNotBlank()) {
            "You've arrived at $destinationName"
        } else {
            "You've arrived at your destination"
        }

        // Close button — dismiss dialog and return to main screen
        dialogView.findViewById<android.widget.Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
            sharedDestinationViewModel.requestClearDestination()
            findNavController().popBackStack()
        }

        dialog.show()

        // Animate the dialog content: scale up + fade in with overshoot bounce
        dialogView.scaleX = 0.5f
        dialogView.scaleY = 0.5f
        dialogView.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(dialogView, View.SCALE_X, 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(dialogView, View.SCALE_Y, 0.5f, 1f)
        val fadeIn = ObjectAnimator.ofFloat(dialogView, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn)
            duration = 400
            interpolator = OvershootInterpolator(1.2f)
            start()
        }

        // Animate the checkmark icon with a delayed bounce
        val icon = dialogView.findViewById<android.widget.ImageView>(R.id.successIcon)
        icon.scaleX = 0f
        icon.scaleY = 0f

        val iconScaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 0f, 1f)
        val iconScaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 0f, 1f)

        AnimatorSet().apply {
            playTogether(iconScaleX, iconScaleY)
            duration = 500
            startDelay = 200
            interpolator = OvershootInterpolator(2.5f)
            start()
        }
    }

    // -----------------------------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
