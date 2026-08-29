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

        // Setup custom map control buttons
        binding.btnZoomIn.setOnClickListener {
            googleMap?.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut.setOnClickListener {
            googleMap?.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.zoomOut())
        }
        binding.btnMyLocation.setOnClickListener {
            recenterMapToUserLocation()
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
                recenterMapToUserLocation()
            } catch (_: SecurityException) {
                // Permission was revoked between the check and enabling — safe to ignore
            }
        }

        // Drop a marker only when the user explicitly taps a spot.
        map.setOnMapClickListener { latLng ->
            pendingPoint = latLng
            pickedMarker?.remove()
            pickedMarker = map.addMarker(MarkerOptions().position(latLng))
            binding.confirmButton.isEnabled = true
        }
    }

    private fun recenterMapToUserLocation() {
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext())
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null && googleMap != null) {
                        googleMap?.animateCamera(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                LatLng(loc.latitude, loc.longitude), 15f
                            )
                        )
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    // -----------------------------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        if (!confirmed) {
            viewModel.consume()
        }
        _binding = null
    }
}
