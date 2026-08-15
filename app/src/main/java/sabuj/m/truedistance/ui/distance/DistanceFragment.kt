package sabuj.m.truedistance.ui.distance

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.databinding.FragmentDistanceBinding

/** §6.1.1 — Main Screen: destination selection + Start Tracking. */
@AndroidEntryPoint
class DistanceFragment : Fragment() {

    private var _binding: FragmentDistanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DistanceViewModel by viewModels()

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

        binding.startTrackingButton.setOnClickListener {
            // §6.1.1 — validated by isStartEnabled; TrackingFragment reads the
            // selected destination via a shared ViewModel or nav args.
            findNavController().navigate(
                sabuj.m.truedistance.R.id.action_distance_to_tracking
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.startTrackingButton.isEnabled = state.isStartEnabled
                    binding.toLabel.text = state.selectedDestination?.let {
                        "To: ${it.name}"
                    } ?: "To: (choose a destination)"
                }
            }
        }

        // TODO: wire destinationSearchBox to Places Autocomplete (§6.1.1a)
        // TODO: wire mapPickButton to a map-tap picker (§6.1.1b)
        // TODO: wire savedLocationDropdown to viewModel.selectSavedLocation() (§6.1.1c)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
