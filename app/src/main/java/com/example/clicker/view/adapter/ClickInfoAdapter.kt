package com.example.clicker.view.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clicker.R
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.LayoutClickInfoBinding
import com.example.clicker.view.dialog.EditTextDialog
import com.example.clicker.view.dialog.EditTextDialogDto
import com.example.clicker.viewmodel.analyze.AnalyzeViewModel

class ClickInfoAdapter(private val databaseViewModel: AnalyzeViewModel, private val context: Context,
                       private var data: ClickVideoListWithClickInfo, private val clickListener: (position : Int) -> Unit) :
    RecyclerView.Adapter<ClickInfoAdapter.ClickInfoViewHolder>() {


    private lateinit var items : List<ClickInfo>
    private lateinit var dialog : EditTextDialog
    private var editScoreInfoPosition : Int? = null
    init {
        dialog = EditTextDialog(context, EditTextDialogDto("Enter a description of the score.", "description")){
            data.clickInfoList[editScoreInfoPosition!!].clickDescription = it
            
            // 데이터베이스에 저장되지 않은 영상인 경우 자동으로 저장
            if (data.clickVideoListID == 0) {
                Log.d(TAG, "Auto-saving video data before updating memo")
                // 새로운 영상 데이터를 먼저 저장
                databaseViewModel.insertVideoData(
                    success = {
                        Log.d(TAG, "Video data saved successfully")
                    },
                    failed = {
                        Log.e(TAG, "Failed to save video data")
                    }
                )
            } else {
                // 이미 저장된 영상인 경우 바로 업데이트
                databaseViewModel.update(data)
            }
            
            // UI 갱신을 위해 notifyItemChanged 호출
            notifyItemChanged(editScoreInfoPosition!!)
            dialog.cancel()
        }
        items = data.clickInfoList
    }
    class ClickInfoViewHolder(val binding: LayoutClickInfoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClickInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_click_info, parent, false)
        val viewHolder = ClickInfoViewHolder(LayoutClickInfoBinding.bind(view))

        viewHolder.binding.memoEditText.setOnClickListener {
            editScoreInfoPosition = viewHolder.adapterPosition
            clickListener.invoke(editScoreInfoPosition!!)
            //clickListener.invoke(items) //views.adapterposition
            dialog.show()
        }
        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ClickInfoViewHolder, position: Int) {
        holder.binding.clickInfo = items[position]
    }
}