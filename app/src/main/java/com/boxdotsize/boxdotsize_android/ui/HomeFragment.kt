package com.boxdotsize.boxdotsize_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boxdotsize.boxdotsize_android.R
import com.boxdotsize.boxdotsize_android.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btUniBoxAnalyze.setOnClickListener {
            findNavController().navigate(R.id.uniBoxSizeMeasureFragment)
        }

        binding.btMutliBoxAnalyze.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_multiBoxSizeMeasureFragment)
        }
        binding.btnTest.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_testFragment)
        }
        binding.btnGoRecord.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_recordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}