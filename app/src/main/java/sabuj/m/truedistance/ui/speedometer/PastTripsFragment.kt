package sabuj.m.truedistance.ui.speedometer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentPastTripsBinding

/**
 * §6.2.2 — Past Trips Screen listing completed Speedometer trips.
 */
@AndroidEntryPoint
class PastTripsFragment : Fragment(), MenuProvider {

    private var _binding: FragmentPastTripsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PastTripsViewModel by viewModels()
    private lateinit var adapter: PastTripsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPastTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        adapter = PastTripsAdapter(
            onItemClick = { trip ->
                viewModel.toggleExpand(trip)
            },
            onDeleteClick = { trip ->
                viewModel.delete(trip)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.unit = state.unit
                    adapter.decimalPrecision = state.decimalPrecision
                    adapter.autoMetersUnder1km = state.autoMetersUnder1km
                    adapter.submitList(state.items)

                    if (state.items.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(Menu.NONE, MENU_CLEAR_ALL, Menu.NONE, R.string.clear_all)
            .setIcon(R.drawable.ic_delete)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            MENU_CLEAR_ALL -> {
                showClearAllConfirmation()
                true
            }
            else -> false
        }
    }

    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_all_trips_title)
            .setMessage(R.string.clear_all_trips_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearAll()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MENU_CLEAR_ALL = 101
    }
}
