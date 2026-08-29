package sabuj.m.truedistance.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sabuj.m.truedistance.BuildConfig
import sabuj.m.truedistance.R
import sabuj.m.truedistance.database.GpsAccuracyMode
import sabuj.m.truedistance.database.ThemeMode
import sabuj.m.truedistance.database.UnitPreference
import sabuj.m.truedistance.databinding.FragmentSettingsBinding

/**
 * §6.3 Settings Screen — User preferences configuration (§6.3.1) and About application information (§6.3.2).
 *
 * Configurable Settings:
 * - Measurement Units: Pill toggle selector (KM vs Miles)
 * - App Appearance Theme: Pill toggle selector (System Default, Light Mode, Dark Mode)
 * - GPS Accuracy: Pill toggle selector (High Accuracy, Balanced Power, Device Only)
 * - Location Frequency: Pill toggle selector (1s, 3s, 5s, 10s updates)
 * - Auto-Meters switch: Toggles displaying distances under 1km as meters
 * - Background Tracking switch: Enables/disables continuous location polling in foreground service
 *
 * About True Distance:
 * - Version readout with versionCode and versionName from BuildConfig
 * - In-app Privacy Policy dialog
 * - Open source GitHub repository shortcut link
 * - Developer profile external link
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private var isApplyingState = false // Guards against recursive feedback loops while applying StateFlow

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        // Observe reactive preferences and bind pill toggles and switches
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> applyState(state) }
            }
        }

        // Display app version from Gradle BuildConfig
        binding.versionText.text = getString(
            R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
        )

        // In-App Privacy Policy Dialog
        binding.privacyPolicyCard.setOnClickListener {
            showInAppPrivacyPolicyDialog()
        }

        // Open Source Repository (GitHub link)
        binding.githubCard.setOnClickListener {
            openUrl("https://github.com/sabujdip01/TrueDistance.git")
        }

        // Developer Credit (about.me link)
        binding.developerCreditCard.setOnClickListener {
            openUrl("https://about.me/sabujdip01")
        }
    }

    /**
     * Attaches change listeners to pill toggle groups and material switches.
     */
    private fun setupListeners() {
        // Unit Pill Toggle Selector (KM / Miles)
        binding.unitToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || isApplyingState) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnUnitKm -> viewModel.setUnit(UnitPreference.KM)
                R.id.btnUnitMiles -> viewModel.setUnit(UnitPreference.MILES)
            }
        }

        // Theme Pill Toggle Selector (System / Light / Dark)
        binding.themeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || isApplyingState) return@addOnButtonCheckedListener
            val (mode, nightMode) = when (checkedId) {
                R.id.btnThemeLight -> Pair(ThemeMode.LIGHT, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
                R.id.btnThemeDark -> Pair(ThemeMode.DARK, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
                else -> Pair(ThemeMode.SYSTEM, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            viewModel.setTheme(mode)
            if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != nightMode) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }

        // GPS Accuracy Pill Toggle Selector
        binding.accuracyToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || isApplyingState) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnAccHigh -> viewModel.setGpsAccuracyMode(GpsAccuracyMode.HIGH)
                R.id.btnAccBalanced -> viewModel.setGpsAccuracyMode(GpsAccuracyMode.BALANCED)
                R.id.btnAccDevice -> viewModel.setGpsAccuracyMode(GpsAccuracyMode.DEVICE_ONLY)
            }
        }

        // Update Frequency Pill Toggle Selector (1s, 2s, 3s, 5s)
        binding.frequencyToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || isApplyingState) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnFreq1s -> viewModel.setUpdateFrequencySeconds(1)
                R.id.btnFreq2s -> viewModel.setUpdateFrequencySeconds(2)
                R.id.btnFreq3s -> viewModel.setUpdateFrequencySeconds(3)
                R.id.btnFreq5s -> viewModel.setUpdateFrequencySeconds(5)
            }
        }

        // Auto-meters switch
        binding.autoMetersSwitch.setOnCheckedChangeListener { _, checked ->
            if (!isApplyingState) viewModel.setAutoMetersUnder1km(checked)
        }

        // Keep screen on switch
        binding.keepScreenOnSwitch.setOnCheckedChangeListener { _, checked ->
            if (!isApplyingState) viewModel.setKeepScreenOn(checked)
        }
    }

    private fun applyState(state: SettingsUiState) {
        isApplyingState = true

        // Unit Toggle State
        val unitButtonId = when (state.unit) {
            UnitPreference.MILES -> R.id.btnUnitMiles
            else -> R.id.btnUnitKm
        }
        if (binding.unitToggleGroup.checkedButtonId != unitButtonId) {
            binding.unitToggleGroup.check(unitButtonId)
        }

        // Theme Toggle State
        val themeButtonId = when (state.theme) {
            ThemeMode.SYSTEM -> R.id.btnThemeSystem
            ThemeMode.LIGHT -> R.id.btnThemeLight
            ThemeMode.DARK -> R.id.btnThemeDark
        }
        if (binding.themeToggleGroup.checkedButtonId != themeButtonId) {
            binding.themeToggleGroup.check(themeButtonId)
        }

        // GPS Accuracy Toggle State
        val accButtonId = when (state.gpsAccuracyMode) {
            GpsAccuracyMode.HIGH -> R.id.btnAccHigh
            GpsAccuracyMode.BALANCED -> R.id.btnAccBalanced
            GpsAccuracyMode.DEVICE_ONLY -> R.id.btnAccDevice
        }
        if (binding.accuracyToggleGroup.checkedButtonId != accButtonId) {
            binding.accuracyToggleGroup.check(accButtonId)
        }

        // Frequency Toggle State
        val freqButtonId = when (state.updateFrequencySeconds) {
            1 -> R.id.btnFreq1s
            2 -> R.id.btnFreq2s
            3 -> R.id.btnFreq3s
            5 -> R.id.btnFreq5s
            else -> R.id.btnFreq3s
        }
        if (binding.frequencyToggleGroup.checkedButtonId != freqButtonId) {
            binding.frequencyToggleGroup.check(freqButtonId)
        }

        // Switch States
        if (binding.autoMetersSwitch.isChecked != state.autoMetersUnder1km) {
            binding.autoMetersSwitch.isChecked = state.autoMetersUnder1km
        }
        if (binding.keepScreenOnSwitch.isChecked != state.keepScreenOn) {
            binding.keepScreenOnSwitch.isChecked = state.keepScreenOn
        }

        isApplyingState = false
    }

    private fun showInAppPrivacyPolicyDialog() {
        val policyText = """
            True Distance Privacy Policy

            1. Location Data:
            True Distance uses precise device GPS location solely to compute distance, speed, and navigation routes. Location data is processed locally on your device and is never uploaded, sold, or shared with third parties.

            2. Storage:
            Your saved locations and trip history are stored securely in a local Room database on your device.

            3. Foreground Services:
            Foreground services with persistent notifications are utilized exclusively to maintain continuous location updates while tracking trips in the background.

            4. Contact & Support:
            For queries or feedback, visit https://about.me/sabujdip01
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Privacy Policy")
            .setMessage(policyText)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) { }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
