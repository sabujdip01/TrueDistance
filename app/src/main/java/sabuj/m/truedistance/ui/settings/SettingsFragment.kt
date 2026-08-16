package sabuj.m.truedistance.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.BuildConfig
import sabuj.m.truedistance.R
import sabuj.m.truedistance.database.GpsAccuracyMode
import sabuj.m.truedistance.database.ThemeMode
import sabuj.m.truedistance.database.UnitPreference
import sabuj.m.truedistance.databinding.FragmentSettingsBinding

/** §6.3 Settings — Preferences (§6.3.1) + About (§6.3.2). */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private var isApplyingState = false // guards against feedback loops while binding spinners

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> applyState(state) }
            }
        }

        binding.versionText.text = getString(
            R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
        )

        binding.privacyPolicyRow.setOnClickListener {
            // TODO: point at the actual hosted privacy policy URL once published
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy")))
        }
    }

    private fun setupSpinners() {
        binding.themeSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            arrayOf(getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system))
        )
        binding.unitSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            arrayOf(getString(R.string.unit_km), getString(R.string.unit_miles), getString(R.string.unit_both))
        )
        binding.accuracySpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            arrayOf(getString(R.string.accuracy_high), getString(R.string.accuracy_balanced), getString(R.string.accuracy_device_only))
        )
        binding.frequencySpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            arrayOf("1s", "3s", "5s", "10s")
        )
    }

    private fun setupListeners() {
        binding.themeSpinner.onItemSelectedListener = onSelected { index ->
            viewModel.setTheme(ThemeMode.entries[index])
        }
        binding.unitSpinner.onItemSelectedListener = onSelected { index ->
            viewModel.setUnit(UnitPreference.entries[index])
        }
        binding.accuracySpinner.onItemSelectedListener = onSelected { index ->
            viewModel.setGpsAccuracyMode(GpsAccuracyMode.entries[index])
        }
        binding.frequencySpinner.onItemSelectedListener = onSelected { index ->
            viewModel.setUpdateFrequencySeconds(listOf(1, 3, 5, 10)[index])
        }

        binding.autoMetersSwitch.setOnCheckedChangeListener { _, checked ->
            if (!isApplyingState) viewModel.setAutoMetersUnder1km(checked)
        }
        binding.backgroundTrackingSwitch.setOnCheckedChangeListener { _, checked ->
            if (!isApplyingState) viewModel.setBackgroundTrackingEnabled(checked)
        }
    }

    private fun applyState(state: SettingsUiState) {
        isApplyingState = true
        binding.themeSpinner.setSelection(ThemeMode.entries.indexOf(state.theme))
        binding.unitSpinner.setSelection(UnitPreference.entries.indexOf(state.unit))
        binding.accuracySpinner.setSelection(GpsAccuracyMode.entries.indexOf(state.gpsAccuracyMode))
        binding.frequencySpinner.setSelection(listOf(1, 3, 5, 10).indexOf(state.updateFrequencySeconds).coerceAtLeast(0))
        binding.autoMetersSwitch.isChecked = state.autoMetersUnder1km
        binding.backgroundTrackingSwitch.isChecked = state.backgroundTrackingEnabled
        isApplyingState = false
    }

    private fun onSelected(onSelect: (Int) -> Unit) =
        object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isApplyingState) onSelect(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
