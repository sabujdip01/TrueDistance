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

@AndroidEntryPoint
class MapPickerFragment : Fragment(), com.google.android.gms.maps.OnMapReadyCallback {

    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapPickerViewModel by activityViewModels()
    private var googleMap: GoogleMap? = null
    private var pickedMarker: com.google.android.gms.maps.model.Marker? = null

    /** The point currently under the camera crosshair — local only until confirmed. */
    private var pendingPoint: LatLng? = null
    private var confirmed = false

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
            pendingPoint?.let { point ->
                confirmed = true
                viewModel.setPickedPoint(point)
            }
            findNavController().popBackStack()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable zoom controls (+/- buttons)
        map.uiSettings.isZoomControlsEnabled = true

        // Enable My Location blue dot + built-in "my location" button (repositioned to bottom-right with +/-)
        if (LocationPermissionHelper.hasForegroundLocationPermission(requireContext())) {
            try {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
                repositionMyLocationButton()
            } catch (_: SecurityException) { }
        }

        // Marker only appears when user explicitly taps a location
        map.setOnMapClickListener { latLng ->
            pendingPoint = latLng
            pickedMarker?.remove()
            pickedMarker = map.addMarker(MarkerOptions().position(latLng))
            binding.confirmButton.isEnabled = true
        }
    }

    private fun repositionMyLocationButton() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.pickerMapContainer) as? SupportMapFragment
        val mapView = mapFragment?.view ?: return
        
        val adjustPosition = {
            val locationButton = findMyLocationButton(mapView)
            if (locationButton != null && locationButton.layoutParams is android.widget.RelativeLayout.LayoutParams) {
                val rlp = locationButton.layoutParams as android.widget.RelativeLayout.LayoutParams
                rlp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP, 0)
                rlp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM, android.widget.RelativeLayout.TRUE)
                rlp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_RIGHT, android.widget.RelativeLayout.TRUE)
                rlp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END, android.widget.RelativeLayout.TRUE)
                val density = resources.displayMetrics.density
                // Position directly above the +/- zoom buttons
                rlp.bottomMargin = (90 * density).toInt()
                rlp.marginEnd = (12 * density).toInt()
                locationButton.layoutParams = rlp
            }
        }

        mapView.post(adjustPosition)
        mapView.postDelayed(adjustPosition, 300)
    }

    private fun findMyLocationButton(view: View): View? {
        if (view.contentDescription?.toString()?.contains("location", ignoreCase = true) == true ||
            view.tag?.toString()?.contains("location", ignoreCase = true) == true ||
            view.id == 2) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        // If the user left without confirming, clear any stale picked point
        if (!confirmed) {
            viewModel.consume()
        }
        _binding = null
    }
}

