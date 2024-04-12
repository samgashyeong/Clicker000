package com.clicker000.clicker.view.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clicker000.clicker.R
import com.clicker000.clicker.data.database.ClickInfo
import com.clicker000.clicker.data.database.ClickVideoListWithClickInfo
import com.clicker000.clicker.databinding.LayoutClickInfoBinding
import com.clicker000.clicker.view.dialog.EditTextDialog
import com.clicker000.clicker.view.dialog.EditTextDialogDto
import com.clicker000.clicker.viewmodel.MainDatabaseViewModel

class ClickInfoAdapter(private val databaseViewModel: MainDatabaseViewModel, private val context: Context,
    private var data : ClickVideoListWithClickInfo, private val clickListener: (position : Int) -> Unit) :
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
            Log.d(TAG, "onCreateViewHolder: 포지션뭐시기 ${editScoreInfoPosition}")
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