package com.example.clicker.view.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.example.clicker.databinding.FragmentClickInfoBinding
import com.example.clicker.view.adapter.ClickInfoAdapter
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.viewmodel.analyze.AnalyzeViewModel

class ClickInfoFragment : Fragment() {
    private lateinit var binding: FragmentClickInfoBinding
    private val viewModel: AnalyzeViewModel by activityViewModels()
    //private val databaseViewModel : MainDatabaseViewModel by activityViewModels()
    private lateinit var editTextDialog: EditTextDialog
    private var previousScrollPosition = -2 // 초기값을 -2로 설정하여 -1과 구분
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClickInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var dataChangeIndex : Int? = null
        viewModel.videoInfo?.observe(viewLifecycleOwner, Observer {
            binding.recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ClickInfoAdapter(viewModel, requireContext() , viewModel.videoInfo.value!!){
                    dataChangeIndex = it
                }
            }
            // 새로운 비디오 로드 시 스크롤 위치 초기화
            previousScrollPosition = -2
            Log.d(TAG, "VideoInfo changed - reset scroll position")
        })

        viewModel.nowPosition.observe(viewLifecycleOwner, Observer { position ->
            // position이 실제로 변경되었고, 유효한 범위에 있을 때만 스크롤
            val clickInfoList = viewModel.clickInfo.value
            if (position != previousScrollPosition && position >= 0 && clickInfoList != null && position < clickInfoList.size) {
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
                Log.d(TAG, "Scrolling to position: $position (previous: $previousScrollPosition)")
                previousScrollPosition = position // 이전 position 업데이트
            } else {
                Log.d(TAG, "Skipping scroll - position: $position, previous: $previousScrollPosition, listSize: ${clickInfoList?.size}")
            }
        })

        viewModel.readAllData.observe(viewLifecycleOwner, Observer {
            binding.recycler.apply {
                Log.d(TAG, "onViewCreated: ${dataChangeIndex}")
                //dataChangeIndex?.let { it1 -> adapter?.notifyItemChanged(it1) }
                adapter!!.notifyDataSetChanged()
            }
        })
    }
}