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
            databaseViewModel.update(data)
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