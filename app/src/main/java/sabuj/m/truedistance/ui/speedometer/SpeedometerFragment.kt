package sabuj.m.truedistance.ui.speedometer

import android.Manifest
import android.content.Context
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
import com.google.android.gms.maps.model.BitmapDescriptor
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
import sabuj.m.truedistance.utils.DistanceCalculator

/**
 * §6.2 Speedometer Screen — Live trip tracking with map, floating live speed readout,
 * statistics card, and Start / Pause / Stop controls.
 *
 * Responsibilities:
 * 1. Hosts the interactive Google Map instance with blue live location marker and breadcrumb polyline.
 * 2. Provides floating live speed readout and trip statistics (max speed, avg speed, distance, elapsed timer).
 * 3. Controls trip state transitions (Start, Pause, Resume, Stop) via SpeedometerService.
 * 4. Displays unified zoom and my-location controls anchored in the bottom-right corner.
 * 5. Provides shortcut to Past Trips history screen.
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

    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    // Permission request contract for runtime location permissions
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchInitialLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedometerBinding.inflate(inflater, container, false)
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SupportMapFragment asynchronously
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupButtons()
        observeUiState()
        checkPermissions()
    }

    /**
     * Checks if location permissions are granted; prompts user if missing.
     */
    private fun checkPermissions() {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Queries last known device location to center the map before a trip starts.
     */
    @android.annotation.SuppressLint("MissingPermission")
    private fun fetchInitialLocation() {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && googleMap != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    updateCurrentMarker(latLng, viewModel.uiState.value.isTracking)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            }
        }
    }

    private var isFollowLocationMode = false

    /**
     * Sets up click listeners for trip controls, history navigation, and map zoom buttons.
     */
    private fun setupButtons() {
        // Navigate to Past Trips history screen
        binding.historyButton.setOnClickListener {
            findNavController().navigate(R.id.action_speedometer_to_pastTrips)
        }

        // Start Trip action
        binding.startTripButton.setOnClickListener {
            checkPermissionsAndStart()
        }

        // Pause / Resume Trip action
        binding.pauseResumeButton.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.isPaused) {
                viewModel.resumeTrip(requireContext())
            } else {
                viewModel.pauseTrip(requireContext())
            }
        }

        // Stop Trip action: saves trip and displays confirmation toast
        binding.stopTripButton.setOnClickListener {
            viewModel.stopTrip(requireContext())
            isFollowLocationMode = false
            binding.btnBackToOverview.visibility = View.GONE
            android.widget.Toast.makeText(requireContext(), getString(R.string.trip_saved), android.widget.Toast.LENGTH_SHORT).show()
        }

        // Unified floating map controls stack
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
            } else {
                fetchInitialLocation()
            }
        }

        // Return to Overview / Fit All Markers (WhatsApp feature)
        binding.btnBackToOverview.setOnClickListener {
            isFollowLocationMode = false
            binding.btnBackToOverview.visibility = View.GONE
            fitOverviewBounds(viewModel.uiState.value)
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
        map.uiSettings.isMyLocationButtonEnabled = false

        if (sabuj.m.truedistance.utils.LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            try {
                map.isMyLocationEnabled = true
            } catch (_: SecurityException) {}
        }

        polyline = map.addPolyline(
            PolylineOptions()
                .color(0xFF00796B.toInt()) // Dark teal
                .width(8f)
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
        )

        fetchInitialLocation()
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

    private var startMarker: com.google.android.gms.maps.model.Marker? = null

    private fun vectorToBitmapDescriptor(context: Context, @androidx.annotation.DrawableRes vectorResId: Int): BitmapDescriptor {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId)
            ?: return BitmapDescriptorFactory.defaultMarker()
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = android.graphics.Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun updateCurrentMarker(latLng: LatLng, isTracking: Boolean) {
        val map = googleMap ?: return
        if (isTracking) {
            val carIcon = vectorToBitmapDescriptor(requireContext(), R.drawable.ic_car)
            if (currentMarker == null) {
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.you))
                        .icon(carIcon)
                        .anchor(0.5f, 0.5f)
                )
            } else {
                currentMarker?.position = latLng
                currentMarker?.setIcon(carIcon)
                currentMarker?.setAnchor(0.5f, 0.5f)
            }
        } else {
            val redMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            if (currentMarker == null) {
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.you))
                        .icon(redMarker)
                        .anchor(0.5f, 1.0f)
                )
            } else {
                currentMarker?.position = latLng
                currentMarker?.setIcon(redMarker)
                currentMarker?.setAnchor(0.5f, 1.0f)
            }
        }
    }

    private fun updateMap(state: SpeedometerUiState) {
        val map = googleMap ?: return

        // Update current location marker (Red marker before start, Small Car icon after start)
        state.currentLocation?.let { latLng ->
            updateCurrentMarker(latLng, state.isTracking)

            if (isFirstFix && state.isTracking) {
                // On trip start: zoom to highest level (18.5f) centered on user location
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.5f))
                isFirstFix = false
            }
        }

        // Add/update Start Flag marker when tracking starts
        if (state.isTracking && state.pathPoints.isNotEmpty()) {
            val startPoint = state.pathPoints.first()
            if (startMarker == null) {
                val flagIcon = vectorToBitmapDescriptor(requireContext(), R.drawable.ic_race_flag)
                startMarker = map.addMarker(
                    MarkerOptions()
                        .position(startPoint)
                        .title("Start")
                        .icon(flagIcon)
                        .anchor(0.5f, 0.5f)
                )
            } else {
                startMarker?.position = startPoint
            }
        } else if (!state.isTracking) {
            startMarker?.remove()
            startMarker = null
        }

        // Update polyline path and adjust camera (Overview mode vs Follow mode)
        if (state.pathPoints.isNotEmpty()) {
            polyline?.points = state.pathPoints

            if (state.isTracking && !state.isPaused) {
                if (isFollowLocationMode) {
                    // Zoomed in follow mode: keep user location centered without auto-zooming out
                    state.currentLocation?.let {
                        map.animateCamera(CameraUpdateFactory.newLatLng(it))
                    }
                } else {
                    // Default Overview mode: fit all covered path points and markers
                    fitOverviewBounds(state)
                }
            }
        } else {
            polyline?.points = emptyList()
        }
    }

    /**
     * WhatsApp-style overview bounds calculation:
     * Calculates map padding taking into account top speed card, bottom card, and right-side floating control stack,
     * so neither start marker, polyline path, nor current location marker is obscured.
     */
    private fun fitOverviewBounds(state: SpeedometerUiState) {
        val map = googleMap ?: return
        val density = resources.displayMetrics.density

        if (state.pathPoints.size > 1) {
            val builder = LatLngBounds.Builder()
            state.pathPoints.forEach { builder.include(it) }
            state.currentLocation?.let { builder.include(it) }

            try {
                val bounds = builder.build()
                val start = state.pathPoints.first()
                val current = state.currentLocation ?: state.pathPoints.last()
                val spreadMeters = DistanceCalculator.haversineMeters(
                    start.latitude, start.longitude,
                    current.latitude, current.longitude
                )

                if (spreadMeters > 15.0) {
                    // Top padding for speed overlay card
                    val topPadding = if (binding.speedOverlayCard.height > 0) {
                        binding.speedOverlayCard.bottom + (20 * density).toInt()
                    } else {
                        (130 * density).toInt()
                    }
                    // Right padding for map control stack (44dp button + 16dp margin + 25dp extra cushion = 85dp)
                    val rightPadding = (85 * density).toInt()
                    val leftPadding = (40 * density).toInt()
                    val bottomPadding = (40 * density).toInt()

                    map.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (30 * density).toInt()))
                } else if (state.currentLocation != null) {
                    map.setPadding(0, (120 * density).toInt(), 0, (20 * density).toInt())
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(state.currentLocation, 18.0f))
                }
            } catch (_: Exception) {
                state.currentLocation?.let {
                    map.animateCamera(CameraUpdateFactory.newLatLng(it))
                }
            }
        } else if (state.currentLocation != null) {
            map.setPadding(0, (120 * density).toInt(), 0, (20 * density).toInt())
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(state.currentLocation, 18.0f))
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
