package com.bluell.roomdecoration.interiordesign.ui.premium

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.interfaces.SelectPlan
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOPlans
import com.bluell.roomdecoration.interiordesign.databinding.ListItemPremiumInappBinding

class InAppAdapter(var items: ArrayList<DTOPlans>, var selPosition:Int, val selectPlan: SelectPlan) :
    RecyclerView.Adapter<InAppAdapter.ViewHolder>() {
    private var context: Context? = null
    private var selectedItem=-1

    class ViewHolder(val binding: ListItemPremiumInappBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        context = parent.context
        val binding = ListItemPremiumInappBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.constraintPlans.isSelected = holder.adapterPosition==selectedItem

        val item = items[position]
        if (!item.isEnabled){
            holder.binding.constraintPlans.isEnabled=false
        }

        if (holder.adapterPosition == selPosition) {
            holder.binding.constraintPlans.strokeColor = holder.itemView.context.resources.getColor(R.color.premium_stroke_selected)
            holder.binding.inAppPrice.setTextColor(holder.itemView.context.resources.getColor(R.color.premium_stroke_selected))
            holder.binding.gemsTxt.setTextColor(holder.itemView.context.resources.getColor(R.color.premium_stroke_selected))
            holder.binding.arrowNext.setColorFilter(holder.itemView.context.resources.getColor(R.color.premium_stroke_selected))
            holder
        } else {
            holder.binding.constraintPlans.strokeColor = holder.itemView.context.resources.getColor(R.color.premium_stroke_unselected)
            holder.binding.inAppPrice.setTextColor(holder.itemView.context.resources.getColor(R.color.bullet_points_color))
            holder.binding.gemsTxt.setTextColor(holder.itemView.context.resources.getColor(R.color.bullet_points_color))
            holder.binding.arrowNext.setColorFilter(holder.itemView.context.resources.getColor(R.color.bullet_points_color))
        }

        holder.itemView.isSelected = position == selPosition

        holder.binding.inAppPrice.text = item.planPrice
        holder.binding.gemsTxt.text=item.gems.toString() + " Gems"



        holder.itemView.setOnClickListener { view ->
            val previousSelectedItemPosition = selectedItem
            selPosition = holder.adapterPosition
            notifyDataSetChanged()
            setSelectedItem(holder.adapterPosition)
            selectPlan.selectedPlan(item)
        }
    }
    fun setSelectedItem(position: Int) {
        notifyItemChanged(selectedItem)
        selectedItem = position
        notifyItemChanged(selectedItem)

    }

    fun newData(newItems:ArrayList<DTOPlans>){
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()

    }
    fun setUpdate(isEnabled:Boolean,position: Int){
        val mutableItems=items
        mutableItems[position].isEnabled=isEnabled
        items=mutableItems
        notifyDataSetChanged()
    }
    override fun getItemCount() = items.size
}