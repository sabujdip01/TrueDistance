package sabuj.m.truedistance.ui.speedometer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentSpeedometerBinding

/**
 * §6.2 Speedometer Screen — Live trip tracking with map, floating live speed readout,
 * statistics card, and Start / Pause / Stop controls.
 */
@AndroidEntryPoint
class SpeedometerFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentSpeedometerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SpeedometerViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var polyline: Polyline? = null
    private var isFirstFix = true

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startTrip(requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedometerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupButtons()
        observeUiState()
    }

    private fun setupButtons() {
        binding.historyButton.setOnClickListener {
            findNavController().navigate(R.id.action_speedometer_to_pastTrips)
        }

        binding.startTripButton.setOnClickListener {
            checkPermissionsAndStart()
        }

        binding.pauseResumeButton.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.isPaused) {
                viewModel.resumeTrip(requireContext())
            } else {
                viewModel.pauseTrip(requireContext())
            }
        }

        binding.stopTripButton.setOnClickListener {
            viewModel.stopTrip(requireContext())
        }

        binding.recenterButton.setOnClickListener {
            val loc = viewModel.uiState.value.currentLocation
            if (loc != null && googleMap != null) {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17f))
            }
        }
    }

    private fun checkPermissionsAndStart() {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            viewModel.startTrip(requireContext())
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isZoomControlsEnabled = false

        polyline = map.addPolyline(
            PolylineOptions()
                .color(0xFF00796B.toInt()) // Dark teal
                .width(8f)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                    updateMap(state)
                }
            }
        }
    }

    private fun updateUi(state: SpeedometerUiState) {
        // Floating live speed readout
        binding.speedText.text = state.formattedSpeed
        binding.speedUnitText.text = state.speedUnitLabel

        // Stats card
        binding.distanceCoveredText.text = state.formattedDistance
        binding.avgSpeedText.text = state.formattedAvgSpeed
        binding.maxSpeedText.text = state.formattedMaxSpeed
        binding.startTimeText.text = state.formattedStartTime
        binding.elapsedText.text = state.formattedElapsedTime

        // Control buttons
        if (state.isTracking) {
            binding.startTripButton.visibility = View.GONE
            binding.activeControlsLayout.visibility = View.VISIBLE
            binding.pauseResumeButton.text = if (state.isPaused) {
                getString(R.string.action_resume)
            } else {
                getString(R.string.action_pause)
            }
        } else {
            binding.startTripButton.visibility = View.VISIBLE
            binding.activeControlsLayout.visibility = View.GONE
            isFirstFix = true
        }
    }

    private fun updateMap(state: SpeedometerUiState) {
        val map = googleMap ?: return

        // Update current location marker
        state.currentLocation?.let { latLng ->
            if (currentMarker == null) {
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.you))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
            } else {
                currentMarker?.position = latLng
            }

            if (isFirstFix && state.isTracking) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                isFirstFix = false
            }
        }

        // Update polyline path
        if (state.pathPoints.isNotEmpty()) {
            polyline?.points = state.pathPoints

            // When tracking with multiple points, smoothly follow user or adjust bounds
            if (state.isTracking && !state.isPaused && state.currentLocation != null) {
                map.animateCamera(CameraUpdateFactory.newLatLng(state.currentLocation))
            }
        } else {
            polyline?.points = emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null
        currentMarker = null
        polyline = null
        _binding = null
    }
}
