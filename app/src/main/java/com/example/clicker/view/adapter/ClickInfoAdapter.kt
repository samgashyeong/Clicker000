package com.example.clicker.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clicker.R
import com.example.clicker.data.database.ClickInfo
import com.example.clicker.databinding.LayoutClickInfoBinding

class ClickInfoAdapter(private var items : List<ClickInfo>, private val clickListener: (item: ClickInfo) -> Unit) :
    RecyclerView.Adapter<ClickInfoAdapter.ClickInfoViewHolder>() {

    class ClickInfoViewHolder(val binding: LayoutClickInfoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClickInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_click_info, parent, false)
        val viewHolder = ClickInfoViewHolder(LayoutClickInfoBinding.bind(view))
        view.setOnClickListener {
            clickListener.invoke(items[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ClickInfoViewHolder, position: Int) {
        holder.binding.clickInfo = items[position]
    }
}