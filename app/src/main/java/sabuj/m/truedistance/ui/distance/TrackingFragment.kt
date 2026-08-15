package sabuj.m.truedistance.ui.distance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import sabuj.m.truedistance.ui.SharedDestinationViewModel
import sabuj.m.truedistance.utils.LocationPermissionHelper

/**
 * §6.1.4 Tracking Screen — live map, current+destination markers, straight-line
 * polyline, live distance readout, Stop button under the map.
 * NOTE: background/foreground-service tracking (persists past this Fragment's
 * lifecycle) is not yet wired — tracking currently stops if this screen is left.
 * Tracked as a follow-up (§6.1.4 background tracking, §12 Tech Notes).
 */
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

        if (!LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            // TODO: request permission (§11.4) before starting; assume granted for now
            return
        }

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

    private fun updateMap(state: TrackingUiState) {
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
                    .color(resources.getColor(sabuj.m.truedistance.R.color.accent_warm, null))
                    .width(6f)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
