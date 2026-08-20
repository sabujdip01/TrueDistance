package sabuj.m.truedistance.ui.distance

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise the map asynchronously — onMapReady() is called when ready
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Stop Tracking button — stops the service and returns to the main screen
        binding.stopButton.setOnClickListener {
            viewModel.stopTracking()
            findNavController().popBackStack()
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

                    // Refresh map markers and polyline
                    updateMap(state)
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

        // Enable +/- zoom control buttons (placed bottom-right by the Maps SDK)
        map.uiSettings.isZoomControlsEnabled = true

        // Enable the blue My Location dot and the native locate button.
        // The button is repositioned to bottom-right so it clusters with the +/- buttons.
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            try {
                map.isMyLocationEnabled = true                   // blue dot at current position
                map.uiSettings.isMyLocationButtonEnabled = true  // locate button visible
                repositionMyLocationButton()                     // move to bottom-right with +/-
            } catch (_: SecurityException) {
                // Permission revoked between check and map setup — safe to ignore
            }
        }

        // Draw markers and polyline using the latest known tracking state
        updateMap(viewModel.uiState.value)
    }

    // -----------------------------------------------------------------------------------------
    // My Location Button Repositioning
    // -----------------------------------------------------------------------------------------

    /**
     * Moves Google Maps' native "My Location" button from its default top-right position
     * to the bottom-right corner, sitting directly above the +/- zoom controls.
     *
     * Applied twice — immediately via [View.post] and again after a 300ms delay — to handle
     * the Maps SDK inflating map controls asynchronously after the first post() runs.
     */
    private fun repositionMyLocationButton() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapContainer) as? SupportMapFragment
        val mapView = mapFragment?.view ?: return

        val adjustPosition = {
            val locationButton = findMyLocationButton(mapView)
            if (locationButton != null &&
                locationButton.layoutParams is android.widget.RelativeLayout.LayoutParams
            ) {
                val rlp = locationButton.layoutParams as android.widget.RelativeLayout.LayoutParams

                // Clear the default top-right anchoring
                rlp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP, 0)

                // Anchor to bottom-right, above the +/- zoom controls
                rlp.addRule(
                    android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM,
                    android.widget.RelativeLayout.TRUE
                )
                rlp.addRule(
                    android.widget.RelativeLayout.ALIGN_PARENT_RIGHT,
                    android.widget.RelativeLayout.TRUE
                )
                rlp.addRule(
                    android.widget.RelativeLayout.ALIGN_PARENT_END,
                    android.widget.RelativeLayout.TRUE
                )

                val density = resources.displayMetrics.density
                // 90dp clears the ~80dp height of the +/- zoom button pair.
                // marginEnd = 0 aligns the location button flush-right with the +/- controls.
                rlp.bottomMargin = (90 * density).toInt()
                rlp.marginEnd = 0

                locationButton.layoutParams = rlp
            }
        }

        mapView.post(adjustPosition)
        mapView.postDelayed(adjustPosition, 300)
    }

    /**
     * Recursively searches the Maps SDK view tree for the "My Location" button.
     *
     * Detection order:
     *  1. Content description contains "location" (Maps SDK sets this, locale-dependent)
     *  2. Tag contains "location"
     *  3. View ID == 2 (stable internal Maps SDK constant)
     */
    private fun findMyLocationButton(view: View): View? {
        if (view.contentDescription?.toString()?.contains("location", ignoreCase = true) == true ||
            view.tag?.toString()?.contains("location", ignoreCase = true) == true ||
            view.id == 2
        ) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = findMyLocationButton(view.getChildAt(i))
                if (child != null) return child
            }
        }
        return null
    }

    // -----------------------------------------------------------------------------------------
    // Map Updates (called on every state emission)
    // -----------------------------------------------------------------------------------------

    /**
     * Updates the map to reflect [state]:
     *  - Adds/moves the current-location marker (azure)
     *  - Adds the destination marker (red) on first fix
     *  - Redraws the polyline connecting the two points
     */
    private fun updateMap(state: TrackingState) {
        val map = googleMap ?: return

        // --- Current location marker ---
        if (state.currentLocation != null) {
            if (currentMarker == null) {
                // First location fix: place marker and animate camera to it
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(state.currentLocation)
                        .title(getString(R.string.you))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(state.currentLocation, 14f))
            } else {
                // Subsequent fixes: just move the existing marker (no camera jump)
                currentMarker?.position = state.currentLocation
            }
        }

        // --- Destination marker (added once, never moved) ---
        if (state.destination != null && destinationMarker == null) {
            destinationMarker = map.addMarker(
                MarkerOptions()
                    .position(state.destination)
                    .title(getString(R.string.destination))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        // --- Polyline from current → destination ---
        if (state.currentLocation != null && state.destination != null) {
            polyline?.remove()  // remove old line before drawing updated one
            polyline = map.addPolyline(
                PolylineOptions()
                    .add(state.currentLocation, state.destination)
                    .color(resources.getColor(R.color.accent_teal, null))
                    .width(6f)
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // Background Tracking Guard
    // -----------------------------------------------------------------------------------------

    /**
     * §6.1.4 / §6.3.1 — If the user has "Background Tracking" disabled in Settings and
     * navigates away from this screen, stop the service rather than continuing silently.
     */
    override fun onStop() {
        super.onStop()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stopIfBackgroundTrackingDisabled()
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
