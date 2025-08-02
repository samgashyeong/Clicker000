package com.example.clicker.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.clicker.R
import com.example.clicker.databinding.FragmentStatisticsBinding
import com.example.clicker.viewmodel.analyze.AnalyzeViewModel
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


class StatisticsFragment : Fragment() {
    private lateinit var binding : FragmentStatisticsBinding
    private val viewModel: AnalyzeViewModel by activityViewModels()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.videoInfo?.observe(viewLifecycleOwner, Observer { videoInfo ->
            if (videoInfo != null) {
                // 비디오 정보가 있는 경우 정상적으로 차트 설정
                binding.chart.apply {
                    val dataset = LineDataSet(viewModel.listChartLiveData.value, "Score")
                    dataset.apply {
                        color = Color.WHITE
                        setCircleColor(Color.rgb(208, 187, 254))
                    }
                    data = LineData(dataset)
                    setChart()
                    invalidate()
                    description = null
                }
            } else {
                // 비디오 정보가 없는 경우 빈 차트 또는 기본 차트 설정
                binding.chart.apply {
                    data = null
                    invalidate()
                    description = null
                }
                Log.d(TAG, "VideoInfo is null - cleared chart data")
            }
        })
        viewModel.listChartLiveData.observe(viewLifecycleOwner, Observer {
            binding.chart.apply {
                //val dataset = LineDataSet(viewModel.listChartLiveData.value)
//                if(viewModel.listChartLiveData.value!!.size != 0){
//                    //Log.d(TAG, "onViewCreated: ${dataSetIndex.label} ${viewModel.listChartLiveData.value?.size!!}")
//                    for(i in 0..viewModel.listChartLiveData.value?.size!!.minus(1)){
//                        data.addEntry(viewModel.listChartLiveData.value!![i], 0)
//                    }
//                }
                val lineDataSet = LineDataSet(viewModel.listChartLiveData.value, "Label") // Create a new dataset with entries
                val lineData = LineData(lineDataSet)
                data = lineData
                invalidate()
                lineDataSet.apply {
                    color = Color.WHITE
                    setCircleColor(Color.rgb(208, 187, 254))
                }
                data.notifyDataChanged()
                binding.chart.notifyDataSetChanged()
                invalidate()
                setChart()
                description = null

            }
        })
    }


    fun setChart(){
        binding.chart.xAxis.apply {
            textColor = Color.WHITE
            axisLineColor = R.color.default_text_color
        }
        binding.chart.axisRight.apply {
            textColor = Color.WHITE
            axisLineColor = R.color.default_text_color
        }
        binding.chart.axisLeft.apply {
            textColor = Color.WHITE
            axisLineColor = R.color.default_text_color
        }
        binding.chart.legend.apply {
            textColor = Color.WHITE
        }

        binding.chart.data.apply {
            setValueTextColor(Color.WHITE)
        }
    }
}