package sabuj.m.truedistance.ui.savedlocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.databinding.FragmentSavedLocationsBinding

/**
 * §6.1.2 Saved Locations Screen — list + swipe-to-delete + add via search/map pick.
 * TODO: wire "Add" flow to Places Autocomplete / map-tap picker; for now the FAB
 * opens a placeholder add dialog (name + manual lat/lng) — replace once the picker
 * exists.
 */
@AndroidEntryPoint
class SavedLocationsFragment : Fragment() {

    private var _binding: FragmentSavedLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedLocationsViewModel by viewModels()
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
                // TODO: pass via a shared ViewModel (SharedDestinationViewModel) or
                // savedStateHandle once DistanceFragment supports receiving it.
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
                viewModel.delete(adapter.currentList[vh.bindingAdapterPosition])
            }
        }).attachToRecyclerView(binding.recyclerView)

        binding.addButton.setOnClickListener {
            // TODO: replace with Places Autocomplete / map-tap picker (§6.1.2)
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
