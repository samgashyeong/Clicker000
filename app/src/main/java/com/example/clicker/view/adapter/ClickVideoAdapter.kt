package com.example.clicker.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.LayoutClickVideoListBinding

class ClickVideoAdapter(private var items : List<ClickVideoListWithClickInfo>, private val clickListener: (item: ClickVideoListWithClickInfo) -> Unit) :
    RecyclerView.Adapter<ClickVideoAdapter.ClickViewHolder>() {

    class ClickViewHolder(val binding: LayoutClickVideoListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClickViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_click_video__list, parent, false)
        val viewHolder = ClickViewHolder(LayoutClickVideoListBinding.bind(view))
        view.setOnClickListener {
            items?.get(viewHolder.adapterPosition)?.let { it1 -> clickListener.invoke(it1) }
        }
        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ClickViewHolder, position: Int) {
        holder.binding.clickVideo = items.get(position)
    }
}

