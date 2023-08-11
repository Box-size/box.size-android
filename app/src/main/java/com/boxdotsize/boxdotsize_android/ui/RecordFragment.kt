package com.boxdotsize.boxdotsize_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.boxdotsize.boxdotsize_android.databinding.FragmentRecordBinding

class RecordFragment:Fragment() {

    private val model: RecordViewModel by viewModels()

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter=RecordRecyclerAdapter()
        binding.rvRecordContainer.apply{
            layoutManager=LinearLayoutManager(requireContext())
            this.adapter=adapter
        }

        binding.tvRecordDeleteAll.setOnClickListener {
            model.deleteAll()
        }

        model.records.observe(viewLifecycleOwner) {
            adapter.setRecordList(it?.reversed() ?: listOf())
        }

        model.changedRecord.observe(viewLifecycleOwner) {
            if(it.isEmpty()) Toast.makeText(requireContext(),"모든 기록이 삭제되었습니다.",Toast.LENGTH_SHORT).show()
            adapter.setRecordList(it?.reversed() ?: listOf())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}