package sabuj.m.truedistance.ui.distance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentStubBinding

/** §6.1.4 Tracking Screen — TODO: map + live markers/line/distance + Stop button */
@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private var _binding: FragmentStubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStubBinding.inflate(inflater, container, false)
        binding.stubLabel.text = getString(R.string.tracking)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
