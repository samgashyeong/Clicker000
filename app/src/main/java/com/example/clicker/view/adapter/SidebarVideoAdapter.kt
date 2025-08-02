package com.example.clicker.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clicker.R
import com.example.clicker.data.database.ClickVideoListWithClickInfo
import com.example.clicker.databinding.LayoutSidebarVideoItemBinding

class SidebarVideoAdapter(
    private var items: List<ClickVideoListWithClickInfo>, 
    private val clickListener: (item: ClickVideoListWithClickInfo) -> Unit
) : RecyclerView.Adapter<SidebarVideoAdapter.SidebarVideoViewHolder>() {

    class SidebarVideoViewHolder(val binding: LayoutSidebarVideoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SidebarVideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_sidebar_video_item, parent, false)
        val viewHolder = SidebarVideoViewHolder(LayoutSidebarVideoItemBinding.bind(view))
        view.setOnClickListener {
            items[viewHolder.adapterPosition].let { clickListener.invoke(it) }
        }
        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SidebarVideoViewHolder, position: Int) {
        holder.binding.clickVideo = items[position]
    }

    fun updateData(newItems: List<ClickVideoListWithClickInfo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
