package sabuj.m.truedistance.ui.mappicker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentMapPickerBinding
import sabuj.m.truedistance.utils.LocationPermissionHelper

/**
 * §6.1.1b (Main Screen "pick on map") / §6.1.2 (Saved Locations "add via map pick") —
 * full-screen map, tap to drop a pin, Confirm button returns the point via
 * MapPickerViewModel and pops back to the caller.
 */
@AndroidEntryPoint
class MapPickerFragment : Fragment(), com.google.android.gms.maps.OnMapReadyCallback {

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapPickerViewModel by activityViewModels()
    private var googleMap: GoogleMap? = null
    private var pickedMarker: com.google.android.gms.maps.model.Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.pickerMapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.confirmButton.isEnabled = false
        binding.confirmButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Enable zoom controls
        map.uiSettings.isZoomControlsEnabled = true
        
        // Enable My Location if permission is granted
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            try {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
                
                // Center on current location
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
                        Log.d("MapPicker", "Centering on lastLocation: $currentLatLng")
                        // Use moveCamera for initial immediate jump
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                    } else {
                        Log.d("MapPicker", "lastLocation is null, requesting fresh fix")
                    }
                    
                    // Always request a fresh fix
                    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 500
                    ).setMaxUpdates(1).build()
                    
                    try {
                        Log.d("MapPicker", "Requesting fresh location fix...")
                        fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
                            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                val last = result.lastLocation
                                if (last != null) {
                                    val currentLatLng = com.google.android.gms.maps.model.LatLng(last.latitude, last.longitude)
                                    Log.d("MapPicker", "Centering on fresh location: $currentLatLng")
                                    // Use animateCamera for the refinement
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                                }
                            }
                        }, android.os.Looper.getMainLooper())
                    } catch (e: SecurityException) {
                        Log.e("MapPicker", "SecurityException requesting location", e)
                    }
                }
            } catch (e: SecurityException) {
                // Should not happen as we checked permission
            }
        }

        map.setOnCameraIdleListener {
            val target = map.cameraPosition.target
            pickedMarker?.remove()
            pickedMarker = map.addMarker(MarkerOptions().position(target))
            viewModel.setPickedPoint(target)
            binding.confirmButton.isEnabled = true
        }

        map.setOnMapClickListener { latLng ->
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
