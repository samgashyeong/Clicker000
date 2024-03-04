package com.clicker000.clicker.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.clicker000.clicker.databinding.FragmentClickInfoBinding
import com.clicker000.clicker.view.adapter.ClickInfoAdapter
import com.clicker000.clicker.view.dialog.EditTextDialog
import com.clicker000.clicker.viewmodel.AnalyzeViewModel
import com.clicker000.clicker.viewmodel.MainDatabaseViewModel

class ClickInfoFragment : Fragment() {
    private lateinit var binding: FragmentClickInfoBinding
    private val viewModel: AnalyzeViewModel by activityViewModels()
    private val databaseViewModel : MainDatabaseViewModel by activityViewModels()
    private lateinit var editTextDialog: EditTextDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClickInfoBinding.inflate(inflater, container, false)
//        editTextDialog = EditTextDialog(requireContext()){
//            databaseViewModel.update()
//        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var dataChangeIndex : Int? = null
        viewModel.videoInfo?.observe(viewLifecycleOwner, Observer {
            binding.recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ClickInfoAdapter(databaseViewModel, requireContext() , viewModel.videoInfo.value!!){
                    dataChangeIndex = it
                }
            }
        })

        viewModel.nowPosition.observe(viewLifecycleOwner, Observer { position ->
            val smoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }

                override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
                    val layoutManager = layoutManager as? LinearLayoutManager
                    return if (layoutManager != null) {
                        super.calculateDyToMakeVisible(view, snapPreference)
                    } else {
                        super.calculateDyToMakeVisible(view, snapPreference)
                    }
                }
            }.apply {
                targetPosition = position
            }

            binding.recycler.layoutManager?.startSmoothScroll(smoothScroller)
        })

        databaseViewModel.readAllData.observe(viewLifecycleOwner, Observer {
            binding.recycler.apply {
                dataChangeIndex?.let { it1 -> adapter?.notifyItemChanged(it1) }
            }
        })
    }
}