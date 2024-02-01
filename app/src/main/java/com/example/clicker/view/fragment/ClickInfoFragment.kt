package com.example.clicker.view.fragment

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicker.R
import com.example.clicker.databinding.FragmentClickInfoBinding
import com.example.clicker.view.activity.AnalyzeActivity
import com.example.clicker.view.adapter.ClickInfoAdapter
import com.example.clicker.view.adapter.ClickVideoAdapter
import com.example.clicker.viewmodel.AnalyzeViewModel

class ClickInfoFragment : Fragment() {
    private lateinit var binding: FragmentClickInfoBinding
    private val viewModel: AnalyzeViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClickInfoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.videoInfo?.observe(viewLifecycleOwner, Observer {
            binding.recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ClickInfoAdapter(viewModel.videoInfo.value!!.clickInfoList){
                    startActivity(Intent(context, AnalyzeActivity::class.java).putExtra("data" ,it))
                    //Toast.makeText(this@ClickVideoListActivity, it.videoInfo.snippet.title, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}