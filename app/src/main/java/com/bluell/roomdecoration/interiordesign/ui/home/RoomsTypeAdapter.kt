package com.bluell.roomdecoration.interiordesign.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.interfaces.RoomTypeSelectionInterface
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel
import com.bluell.roomdecoration.interiordesign.databinding.RoomTypeItemBinding

class RoomsTypeAdapter (private val roomTypes:ArrayList<RoomTypeModel>, private var selectedItemPosition: Int, var clickListner: RoomTypeSelectionInterface): RecyclerView.Adapter<RoomsTypeAdapter.Viewholder>() {
    inner class Viewholder(val binding: RoomTypeItemBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = RoomTypeItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Viewholder(binding)
    }

    override fun getItemCount(): Int {
        return roomTypes.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {

        val rooms = roomTypes[position]
        holder.itemView.isSelected = position == selectedItemPosition
        val context = holder.itemView.context
        with(holder){
            binding.titleText.text = rooms.room_title
            binding.roomIcon.setImageResource(rooms.icon)
            if (rooms.isSelected) {
                binding.select.setImageResource(R.drawable.lan_selected)
            } else {
                binding.select.setImageResource(R.drawable.lan_unselected)
            }
            itemView.setOnClickListener {
                selectedItemPosition = holder.adapterPosition
                notifyDataSetChanged()
                clickListner.selectedRoomType(rooms, position)
            }


            if (adapterPosition == selectedItemPosition) {
                val theme = R.style.room_type_selected
                val themedContext = ContextThemeWrapper(context, theme)
                val styledAttributes = themedContext.obtainStyledAttributes(null, intArrayOf(android.R.attr.src), 0, 0)
                val drawableResId = styledAttributes.getResourceId(0, 0)
                styledAttributes.recycle()

                binding.select.setImageResource(drawableResId)
            } else {
                val theme = R.style.room_type_unselected
                val themedContext = ContextThemeWrapper(context, theme)
                val styledAttributes = themedContext.obtainStyledAttributes(null, intArrayOf(android.R.attr.src), 0, 0)
                val drawableResId = styledAttributes.getResourceId(0, 0)
                styledAttributes.recycle()

                binding.select.setImageResource(drawableResId)
            }

//            if (adapterPosition == selectedItemPosition) {
//                imageView.setImageResource(R.drawable.checked)
//            } else {
//                imageView.setImageResource(R.drawable.unchecked)
//                card.strokeColor = itemView.resources.getColor(R.color.black)
//            }
        }
    }

    fun updateSelection(position: Int){
        selectedItemPosition = position
        notifyDataSetChanged()
    }
}