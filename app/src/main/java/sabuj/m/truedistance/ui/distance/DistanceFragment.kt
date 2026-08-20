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
 * §6.1.1 — Main Screen: destination selection + Start Tracking.
 * Search uses Google Places Autocomplete (§6.1.1a).
 */
@AndroidEntryPoint
class DistanceFragment : Fragment() {

    private var _binding: FragmentDistanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DistanceViewModel by viewModels()
    private val sharedDestinationViewModel: SharedDestinationViewModel by activityViewModels()
    private val mapPickerViewModel: MapPickerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDistanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.savedLocationsButton.setOnClickListener {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_savedLocations
            )
        }

        binding.historyButton.setOnClickListener {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_history
            )
        }

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

        binding.startTrackingButton.setOnClickListener {
            viewModel.uiState.value.selectedDestination?.let {
                sharedDestinationViewModel.setDestination(it)
                findNavController().navigate(
                    sabuj.m.truedistance.R.id.action_distance_to_tracking
                )
            }
        }

        // §6.1.1a — search via Geocoder on IME "search" action
        binding.destinationSearchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.destinationSearchBox.text?.toString().orEmpty()
                if (query.isNotBlank()) viewModel.searchByAddress(query, requireContext())
                true
            } else false
        }

        // §6.1.1b — map-tap picker
        binding.mapPickButton.setOnClickListener {
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_mapPicker
            )
        }

        // §6.1.1c — quick-select dropdown from Saved Locations
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.startTrackingButton.isEnabled = state.isStartEnabled
                    binding.toLabel.text = state.selectedDestination?.let {
                        "To: ${it.name}"
                    } ?: "To: (Choose a Destination)"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // §11.1/§11.2 — no-internet / no-GPS banners, re-checked whenever this
        // screen becomes visible (e.g., returning from system settings).
        val hasInternet = NetworkStatusHelper.isConnected(requireContext())
        val hasLocation = GpsStatusHelper.isLocationEnabled(requireContext())
        binding.noInternetBanner.visibility = if (!hasInternet) View.VISIBLE else View.GONE
        binding.noGpsBanner.visibility = if (!hasLocation) View.VISIBLE else View.GONE
        binding.noGpsBanner.setOnClickListener {
            startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        // §6.1.1b — consume a point picked on MapPickerFragment, if any.
        mapPickerViewModel.consume()?.let { latLng ->
            viewModel.selectPickedPoint(latLng.latitude, latLng.longitude, requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
