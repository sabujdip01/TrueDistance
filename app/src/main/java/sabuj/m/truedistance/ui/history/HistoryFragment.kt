package sabuj.m.truedistance.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import sabuj.m.truedistance.R
import sabuj.m.truedistance.databinding.FragmentStubBinding

/** §6.1.3 Distance History Screen — TODO: grouped list + interval snapshot rows */
@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _binding: FragmentStubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStubBinding.inflate(inflater, container, false)
        binding.stubLabel.text = getString(R.string.distance_history)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
