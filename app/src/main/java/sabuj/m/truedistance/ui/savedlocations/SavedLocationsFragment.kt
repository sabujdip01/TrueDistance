package sabuj.m.truedistance.ui.savedlocations

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentSavedLocationsBinding
import sabuj.m.truedistance.ui.SharedDestinationViewModel
import sabuj.m.truedistance.ui.distance.DestinationSelection
import sabuj.m.truedistance.ui.mappicker.MapPickerViewModel
import sabuj.m.truedistance.utils.PlacesAutocompleteAdapter

/** §6.1.2 Saved Locations Screen — list + swipe-to-delete + add via search/map pick. */
@AndroidEntryPoint
class SavedLocationsFragment : Fragment() {

    private var _binding: FragmentSavedLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedLocationsViewModel by viewModels()
    private val sharedDestinationViewModel: SharedDestinationViewModel by activityViewModels()
    private val mapPickerViewModel: MapPickerViewModel by activityViewModels()
    private lateinit var adapter: SavedLocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SavedLocationAdapter(
            onItemClick = { location ->
                // §6.1.2 — tapping a row returns to Main Screen with it pre-filled.
                sharedDestinationViewModel.setDestination(
                    DestinationSelection(
                        name = location.name,
                        address = location.address,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        savedLocationId = location.id
                    )
                )
                findNavController().popBackStack()
            },
            onDeleteClick = { location -> viewModel.delete(location) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: androidx.recyclerview.widget.RecyclerView,
                                 vh: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                                 target: androidx.recyclerview.widget.RecyclerView.ViewHolder) = false

            override fun onSwiped(vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                    adapter.currentList.getOrNull(pos)?.let { location ->
                        viewModel.delete(location)
                    }
                }
            }
        }).attachToRecyclerView(binding.recyclerView)

        binding.addButton.setOnClickListener { showAddOptionsDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedLocations.collect { locations ->
                    adapter.submitList(locations)
                    binding.emptyState.visibility =
                        if (locations.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // §6.1.2 — consume a point picked on MapPickerFragment, if any, then ask
        // for a name to save it under.
        mapPickerViewModel.consume()?.let { latLng ->
            promptNameAndSave(latLng.latitude, latLng.longitude)
        }
    }

    private fun showAddOptionsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_saved_location)
            .setItems(arrayOf(getString(R.string.add_via_search), getString(R.string.add_via_map))) { _, which ->
                if (which == 0) showSearchDialog()
                else findNavController().navigate(R.id.action_savedLocations_to_mapPicker)
            }
            .show()
    }

    private fun showSearchDialog() {
        val input = AutoCompleteTextView(requireContext()).apply {
            hint = getString(R.string.search_destination_hint)
            threshold = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val autocompleteAdapter = PlacesAutocompleteAdapter(requireContext())
        input.setAdapter(autocompleteAdapter)

        var selectedPlace: com.google.android.libraries.places.api.model.Place? = null

        input.setOnItemClickListener { _, _, position, _ ->
            val prediction = autocompleteAdapter.getItem(position)
            autocompleteAdapter.fetchPlace(prediction) { place ->
                selectedPlace = place
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_via_search)
            .setView(wrapWithPadding(input))
            .setPositiveButton(R.string.save) { _, _ ->
                val place = selectedPlace
                if (place != null && place.latLng != null) {
                    viewModel.addLocation(
                        place.name ?: input.text.toString(),
                        place.address ?: input.text.toString(),
                        place.latLng!!.latitude,
                        place.latLng!!.longitude
                    )
                } else {
                    val query = input.text?.toString().orEmpty()
                    if (query.isNotBlank()) {
                        viewModel.addFromAddress(query, requireContext()) { success ->
                            if (!success) {
                                android.widget.Toast.makeText(
                                    requireContext(), getString(R.string.no_results_found), android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun promptNameAndSave(lat: Double, lng: Double) {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.location_name_hint)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.location_name_hint)
            .setView(wrapWithPadding(input))
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.addFromPoint(lat, lng, input.text?.toString().orEmpty(), requireContext())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun wrapWithPadding(view: View): View {
        val padding = (16 * resources.displayMetrics.density).toInt()
        return LinearLayout(requireContext()).apply {
            setPadding(padding, padding, padding, padding)
            addView(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
