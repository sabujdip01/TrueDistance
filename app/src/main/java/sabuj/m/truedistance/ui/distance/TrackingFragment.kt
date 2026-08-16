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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentTrackingBinding
import sabuj.m.truedistance.service.TrackingState
import sabuj.m.truedistance.ui.SharedDestinationViewModel
import sabuj.m.truedistance.utils.LocationPermissionHelper

/** §6.1.4 Tracking Screen — live map, markers, polyline, distance, Stop under map. */
@AndroidEntryPoint
class TrackingFragment : Fragment(), com.google.android.gms.maps.OnMapReadyCallback {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val sharedDestinationViewModel: SharedDestinationViewModel by activityViewModels()
    private val viewModel: TrackingViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private var currentMarker: com.google.android.gms.maps.model.Marker? = null
    private var destinationMarker: com.google.android.gms.maps.model.Marker? = null
    private var polyline: com.google.android.gms.maps.model.Polyline? = null

    // §10 Permissions — request foreground location + notifications before starting.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            beginTracking()
        } else {
            findNavController().popBackStack() // §11.4 — can't track without permission
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.stopButton.setOnClickListener {
            viewModel.stopTracking()
            findNavController().popBackStack()
        }

        binding.recenterButton.setOnClickListener {
            viewModel.uiState.value.currentLocation?.let {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(it))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.distanceText.text = state.formattedDistance
                    binding.staleIndicator.visibility =
                        if (state.staleFix) View.VISIBLE else View.GONE
                    updateMap(state)
                }
            }
        }

        requestPermissionsAndStart()
    }

    private fun requestPermissionsAndStart() {
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            beginTracking()
        } else {
            val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                perms += Manifest.permission.POST_NOTIFICATIONS
            }
            permissionLauncher.launch(perms.toTypedArray())
        }
    }

    private fun beginTracking() {
        // Avoid re-starting if a session for this destination is already running
        // (e.g., returning to this screen while service is still tracking).
        if (viewModel.uiState.value.isTracking) return

        sharedDestinationViewModel.destination.value?.let { destination ->
            viewModel.startTracking(
                destination.name, destination.latitude, destination.longitude, destination.savedLocationId
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateMap(viewModel.uiState.value)
    }

    private fun updateMap(state: TrackingState) {
        val map = googleMap ?: return

        if (state.currentLocation != null) {
            if (currentMarker == null) {
                currentMarker = map.addMarker(
                    MarkerOptions().position(state.currentLocation).title(getString(R.string.you))
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(state.currentLocation, 14f))
            } else {
                currentMarker?.position = state.currentLocation
            }
        }

        if (state.destination != null && destinationMarker == null) {
            destinationMarker = map.addMarker(
                MarkerOptions().position(state.destination).title(getString(R.string.destination))
            )
        }

        if (state.currentLocation != null && state.destination != null) {
            polyline?.remove()
            polyline = map.addPolyline(
                PolylineOptions()
                    .add(state.currentLocation, state.destination)
                    .color(resources.getColor(R.color.accent_warm, null))
                    .width(6f)
            )
        }
    }

    // §6.1.4 / §6.3.1 — if Background Tracking is disabled, leaving this screen
    // stops the session rather than continuing silently.
    override fun onStop() {
        super.onStop()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stopIfBackgroundTrackingDisabled()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
