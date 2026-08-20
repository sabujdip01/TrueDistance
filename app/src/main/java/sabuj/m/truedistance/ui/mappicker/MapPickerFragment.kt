package sabuj.m.truedistance.ui.mappicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentMapPickerBinding
import sabuj.m.truedistance.utils.LocationPermissionHelper

/**
 * §6.1.1b / §6.1.2 — Full-screen Google Map for picking a destination or a saved location.
 *
 * Flow:
 *  1. User arrives here from Main Screen (pick destination) or Saved Locations (add via map).
 *  2. Map opens at default zoom — user can pan/zoom freely with fingers or +/- buttons.
 *  3. User taps any point on the map → a marker is dropped at that spot.
 *  4. "Confirm Location" button becomes active — tapping it commits the pick to
 *     [MapPickerViewModel] and pops the back stack.
 *  5. If the user presses Back without confirming, [onDestroyView] clears the ViewModel
 *     so no stale point is consumed by the caller's onResume().
 *
 * The native Google Maps "My Location" button is repositioned to sit in the bottom-right
 * corner, directly above the +/- zoom controls, matching standard Google Maps UX.
 */
@AndroidEntryPoint
class MapPickerFragment : Fragment(), com.google.android.gms.maps.OnMapReadyCallback {

    // --- View Binding ---
    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    /**
     * Activity-scoped ViewModel shared with the caller fragment (DistanceFragment or
     * SavedLocationsFragment). Written only on explicit Confirm — never automatically.
     */
    private val viewModel: MapPickerViewModel by activityViewModels()

    // Live GoogleMap reference, null until onMapReady fires
    private var googleMap: GoogleMap? = null

    // The marker currently shown at the user's tapped location (removed and re-added on each tap)
    private var pickedMarker: com.google.android.gms.maps.model.Marker? = null

    /**
     * The LatLng the user last tapped, held locally until they press Confirm.
     * NOT written to the ViewModel until confirmed, so a back-press produces no side-effect.
     */
    private var pendingPoint: LatLng? = null

    /**
     * Tracks whether the user tapped Confirm. Used in [onDestroyView] to decide
     * whether to clear the ViewModel's picked point (i.e., cancel flow).
     */
    private var confirmed = false

    // -----------------------------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise the Google Map asynchronously
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.pickerMapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Confirm button starts disabled — only enabled once the user taps a location
        binding.confirmButton.isEnabled = false
        binding.confirmButton.setOnClickListener {
            pendingPoint?.let { point ->
                // Only now write to the shared ViewModel so the caller can consume it
                confirmed = true
                viewModel.setPickedPoint(point)
            }
            // Pop back to whichever screen launched this picker
            findNavController().popBackStack()
        }
    }

    // -----------------------------------------------------------------------------------------
    // Map Setup
    // -----------------------------------------------------------------------------------------

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Show +/- zoom buttons in the bottom-right corner (Google Maps places them there by default)
        map.uiSettings.isZoomControlsEnabled = true

        // Show the blue "My Location" dot and enable the native My Location button.
        // The button is then repositioned to sit above the +/- controls (bottom-right),
        // matching standard Google Maps UX and keeping all map controls in one cluster.
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            try {
                map.isMyLocationEnabled = true          // shows blue dot at current position
                map.uiSettings.isMyLocationButtonEnabled = true  // shows the locate button
                repositionMyLocationButton()            // move it next to +/- controls
            } catch (_: SecurityException) {
                // Permission was revoked between the check and enabling — safe to ignore
            }
        }

        // Drop a marker only when the user explicitly taps a spot.
        // No marker is placed on load or on pan/zoom — the map starts clean.
        map.setOnMapClickListener { latLng ->
            pendingPoint = latLng          // store locally, NOT in the ViewModel yet
            pickedMarker?.remove()         // clear the previous marker if any
            pickedMarker = map.addMarker(MarkerOptions().position(latLng))
            binding.confirmButton.isEnabled = true   // user has a valid point to confirm
        }
    }

    // -----------------------------------------------------------------------------------------
    // My Location Button Repositioning
    // -----------------------------------------------------------------------------------------

    /**
     * Moves the Google Maps native "My Location" button from its default top-right position
     * to the bottom-right corner, directly above the +/- zoom controls.
     *
     * This is a UI hack that reaches into the Maps SDK internal view hierarchy via
     * [findMyLocationButton]. Two passes are applied (immediate + 300ms delay) to handle
     * the case where the Maps SDK inflates the button after the first post() call.
     */
    private fun repositionMyLocationButton() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.pickerMapContainer) as? SupportMapFragment
        val mapView = mapFragment?.view ?: return

        val adjustPosition = {
            val locationButton = findMyLocationButton(mapView)
            if (locationButton != null &&
                locationButton.layoutParams is android.widget.RelativeLayout.LayoutParams
            ) {
                val rlp = locationButton.layoutParams as android.widget.RelativeLayout.LayoutParams

                // Remove the default top-right anchoring rules
                rlp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP, 0)

                // Anchor to bottom-right to sit above the +/- zoom buttons
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
                // 90dp bottom margin clears the zoom (+/-) buttons beneath it.
                // marginEnd = 0 aligns the location button flush-right with the +/- controls.
                rlp.bottomMargin = (90 * density).toInt()
                rlp.marginEnd = 0

                locationButton.layoutParams = rlp
            }
        }

        // First pass: immediately after the map view is laid out
        mapView.post(adjustPosition)
        // Second pass: Maps SDK may inflate controls asynchronously, retry after 300ms
        mapView.postDelayed(adjustPosition, 300)
    }

    /**
     * Recursively searches the Maps SDK view hierarchy for the "My Location" button.
     *
     * Identification strategy (in order):
     *  1. Content description contains "location" (set by the Maps SDK, locale-dependent)
     *  2. Tag contains "location"
     *  3. View ID == 2 (internal Maps SDK constant, stable across SDK versions tested)
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
    // Cleanup
    // -----------------------------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        // If the user navigated back without confirming (confirmed == false),
        // clear any point that may have been accidentally written to the ViewModel.
        // This prevents the "Name this Location" dialog from appearing on cancel.
        if (!confirmed) {
            viewModel.consume()
        }
        _binding = null
    }
}
