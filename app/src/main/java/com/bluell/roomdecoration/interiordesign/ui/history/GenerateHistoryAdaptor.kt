package com.bluell.roomdecoration.interiordesign.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.ListItemGenerateBinding
import com.bluell.roomdecoration.interiordesign.domain.repo.MyInteriorClicks


class GenerateHistoryAdaptor (private val itmes:MutableList<genericResponseModel>, private val clickListner: MyInteriorClicks): RecyclerView.Adapter<GenerateHistoryAdaptor.Viewholder>() {
    inner class Viewholder(val binding: ListItemGenerateBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ListItemGenerateBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Viewholder(binding)
    }

    override fun getItemCount(): Int {
        return itmes.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = itmes[position]
        val context = holder.itemView.context
        with(holder){
            Glide.with(context).load(item.output!![0]).into(binding.output1)
            Glide.with(context).load(item.output!![1]).into(binding.output2)
            Glide.with(context).load(item.output!![2]).into(binding.output3)
            if (item.output!!.size >3){
                Glide.with(context).load(item.output!![3]).into(binding.output4)
            }

            binding.imageOptions.setOnClickListener {
                clickListner.onMenuItemClick(item,position,binding.more)
            }



            holder.itemView.setOnClickListener {
                clickListner.onItemClick(item,position)
            }
        }
    }


}