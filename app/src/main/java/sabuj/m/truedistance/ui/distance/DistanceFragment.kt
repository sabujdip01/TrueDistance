package sabuj.m.truedistance.ui.distance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.databinding.FragmentDistanceBinding
import sabuj.m.truedistance.ui.SharedDestinationViewModel
import sabuj.m.truedistance.ui.mappicker.MapPickerViewModel
import sabuj.m.truedistance.utils.GpsStatusHelper
import sabuj.m.truedistance.utils.NetworkStatusHelper
import sabuj.m.truedistance.utils.PlacesAutocompleteAdapter

/**
 * §6.1.1 — DistanceFragment is the main entry screen for True Distance.
 *
 * Responsibilities:
 * 1. Supports 3 destination input mechanisms:
 *    - Google Places SDK autocomplete search (§6.1.1a).
 *    - Interactive map picker selection (§6.1.1b).
 *    - Quick selection dropdown from Saved Locations (§6.1.1c).
 * 2. Manages Start Tracking button visual and functional state (disabled with alpha 0.4 until destination is chosen).
 * 3. Monitors real-time Network and GPS hardware availability via top warning banners.
 * 4. Automatically restores the live TrackingFragment if a session is already in progress.
 */
@AndroidEntryPoint
class DistanceFragment : Fragment() {

    private var _binding: FragmentDistanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DistanceViewModel by viewModels()
    private val sharedDestinationViewModel: SharedDestinationViewModel by activityViewModels()
    private val mapPickerViewModel: MapPickerViewModel by activityViewModels()

    @javax.inject.Inject
    lateinit var trackingStateHolder: sabuj.m.truedistance.service.TrackingStateHolder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDistanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If tracking is already running in background, restore tracking screen immediately
        if (trackingStateHolder.state.value.isTracking) {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_tracking
            )
            return
        }

        // Navigate to Saved Locations screen
        binding.savedLocationsButton.setOnClickListener {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_savedLocations
            )
        }

        // Navigate to Distance History screen
        binding.historyButton.setOnClickListener {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_history
            )
        }

        // Set up Google Places Autocomplete search adapter
        val autocompleteAdapter = PlacesAutocompleteAdapter(requireContext())
        binding.destinationSearchBox.setAdapter(autocompleteAdapter)
        binding.destinationSearchBox.setOnItemClickListener { _, _, position, _ ->
            val prediction = autocompleteAdapter.getItem(position)
            autocompleteAdapter.fetchPlace(prediction) { place ->
                val latLng = place.latLng
                if (latLng != null) {
                    viewModel.selectPickedPoint(latLng.latitude, latLng.longitude, requireContext(), place.name)
                }
            }
        }

        // Start Tracking: passes destination coordinates to TrackingFragment
        binding.startTrackingButton.setOnClickListener {
            viewModel.uiState.value.selectedDestination?.let {
                sharedDestinationViewModel.setDestination(it)
                findNavController().navigate(
                    sabuj.m.truedistance.R.id.action_distance_to_tracking
                )
            }
        }

        // Fallback search via Geocoder on keyboard IME "Search" action
        binding.destinationSearchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.destinationSearchBox.text?.toString().orEmpty()
                if (query.isNotBlank()) viewModel.searchByAddress(query, requireContext())
                true
            } else false
        }

        // Open Map Picker screen to tap and choose a location
        binding.mapPickButton.setOnClickListener {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_mapPicker
            )
        }

        // Quick-select dropdown menu from user's Saved Locations
        binding.savedLocationDropdown.setOnClickListener { anchor ->
            val locations = viewModel.uiState.value.savedLocations
            if (locations.isEmpty()) return@setOnClickListener
            val popup = PopupMenu(requireContext(), anchor)
            locations.forEachIndexed { index, location ->
                popup.menu.add(0, index, index, location.name)
            }
            popup.setOnMenuItemClickListener { item ->
                viewModel.selectSavedLocation(locations[item.itemId])
                true
            }
            popup.show()
        }

        // Collect UI state: enable/disable start button and update selected destination label
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.startTrackingButton.isEnabled = state.isStartEnabled
                    binding.startTrackingButton.alpha = if (state.isStartEnabled) 1.0f else 0.4f
                    binding.toLabel.text = state.selectedDestination?.let {
                        "To: ${it.name}"
                    } ?: "To: (Choose a Destination)"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check hardware status banners (No Internet / No GPS)
        val hasInternet = NetworkStatusHelper.isConnected(requireContext())
        val hasLocation = GpsStatusHelper.isLocationEnabled(requireContext())
        binding.noInternetBanner.visibility = if (!hasInternet) View.VISIBLE else View.GONE
        binding.noGpsBanner.visibility = if (!hasLocation) View.VISIBLE else View.GONE
        binding.noGpsBanner.setOnClickListener {
            startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        // Clear destination selection when returning from tracking (stop/reached)
        if (sharedDestinationViewModel.consumeClearRequest()) {
            viewModel.clearDestination()
            binding.destinationSearchBox.setText("")
        }

        // Consume a point picked on MapPickerFragment
        mapPickerViewModel.consume()?.let { latLng ->
            viewModel.selectPickedPoint(latLng.latitude, latLng.longitude, requireContext())
        }

        // Consume destination set by SavedLocationsFragment
        sharedDestinationViewModel.destination.value?.let { dest ->
            viewModel.selectDestination(dest)
            sharedDestinationViewModel.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
