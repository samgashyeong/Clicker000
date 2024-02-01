package com.example.clicker.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.clicker.R
import com.example.clicker.databinding.FragmentStatisticsBinding


class StatisticsFragment : Fragment() {
    private lateinit var binding : FragmentStatisticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

}