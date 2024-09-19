package com.example.clicker.view.fragment

import android.graphics.Color
import android.os.Bundle
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

        viewModel.videoInfo?.observe(viewLifecycleOwner, Observer {
            binding.yourChartName.apply {
                val dataset = LineDataSet(viewModel.listChartLiveData.value,"Score")
                dataset.apply {
                    color = Color.WHITE
                    setCircleColor(Color.rgb(208, 187, 254))
                }
                data = LineData(dataset)
                setChart()
                invalidate()
                description = null
            }

        })
        viewModel.listChartLiveData.observe(viewLifecycleOwner, Observer {
            binding.yourChartName.apply {
                val dataSetIndex : ILineDataSet = data.getDataSetByIndex(0)
                //val dataset = LineDataSet(viewModel.listChartLiveData.value)
                if(viewModel.listChartLiveData.value!!.size != 0){
                    //Log.d(TAG, "onViewCreated: ${dataSetIndex.label} ${viewModel.listChartLiveData.value?.size!!}")
                    for(i in 0..viewModel.listChartLiveData.value?.size!!.minus(1)){
                        data.addEntry(viewModel.listChartLiveData.value!![i], 0)
                    }
                }
                data.notifyDataChanged()
                binding.yourChartName.notifyDataSetChanged()
                invalidate()
                setChart()
                description = null

            }
        })
    }


    fun setChart(){
        binding.yourChartName.xAxis.apply {
            textColor = Color.WHITE
            axisLineColor = R.color.default_text_color
        }
        binding.yourChartName.axisRight.apply {
            textColor = Color.WHITE
            axisLineColor = R.color.default_text_color
        }
        binding.yourChartName.axisLeft.apply {
            textColor = Color.WHITE
            axisLineColor = R.color.default_text_color
        }
        binding.yourChartName.legend.apply {
            textColor = Color.WHITE
        }

        binding.yourChartName.data.apply {
            setValueTextColor(Color.WHITE)
        }
    }
}